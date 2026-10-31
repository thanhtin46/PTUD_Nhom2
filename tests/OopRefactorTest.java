package tests;

import DAO.PhieuSuaChua_DAO;
import Entity.*;
import Service.SuaChuaService;
import java.lang.reflect.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;

/** Chạy độc lập, không mở kết nối tới database thật. */
public class OopRefactorTest {
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }

    private static void invalid(Runnable action) {
        try { action.run(); throw new AssertionError("Expected invalid data"); }
        catch (IllegalArgumentException | ArithmeticException expected) { checks++; }
    }

    private static DuLieuPhieuSuaChua duLieu() {
        return new DuLieuPhieuSuaChua("TN_TEST", "NV_TEST", LocalDate.now().plusDays(2),
                List.of(new ChiTietDichVu("Phụ tùng", "PT_TEST", "Lọc dầu", "Cái", 2, 100000)),
                List.of(new DuLieuPhieuSuaChua.CongViec("Thay lọc", "KTV thử", 50000)));
    }

    /** JDBC giả lập lỗi batch thứ hai để kiểm tra rollback toàn bộ giao dịch. */
    private static class Db implements InvocationHandler {
        boolean auto = true, committed, rolledBack, exported, failWork, missingReceipt, missingRepair;
        String assigned = "NV_TEST";
        int writes, batches;
        final Map<Integer, Object> header = new HashMap<>();
        Connection connection() { return proxy(Connection.class, this); }

        @Override public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            return switch (m.getName()) {
                case "getAutoCommit" -> auto;
                case "setAutoCommit" -> { auto = (boolean) a[0]; yield null; }
                case "commit" -> { committed = true; yield null; }
                case "rollback" -> { rolledBack = true; yield null; }
                case "prepareStatement" -> statement((String) a[0]);
                default -> fallback(m);
            };
        }

        PreparedStatement statement(String sql) {
            return proxy(PreparedStatement.class, (p, m, a) -> {
                if (m.getName().startsWith("set") && sql.startsWith("UPDATE PhieuSuaChua"))
                    header.put((Integer) a[0], a[1]);
                return switch (m.getName()) {
                    case "executeQuery" -> {
                        boolean[] next = {!(missingReceipt && sql.contains("FROM PhieuTiepNhan"))
                                && !(missingRepair && sql.startsWith("SELECT maNV"))};
                        yield proxy(ResultSet.class, (r, rm, ra) -> switch (rm.getName()) {
                            case "next" -> { boolean value = next[0]; next[0] = false; yield value; }
                            case "getInt" -> exported ? 1 : 0;
                            case "getString" -> {
                                if (sql.startsWith("SELECT TOP 1 maPSC")) yield "SC_TEST";
                                if ("maNV".equals(ra[0])) yield assigned;
                                if ("maKH".equals(ra[0])) yield "KH_TEST";
                                yield "VIN_TEST";
                            }
                            default -> fallback(rm);
                        });
                    }
                    case "executeUpdate" -> { writes++; yield missingRepair && sql.startsWith("UPDATE PhieuSuaChua") ? 0 : 1; }
                    case "executeBatch" -> {
                        batches++;
                        if (failWork && sql.contains("ChiTietCongViecSuaChua")) throw new SQLException("Injected work failure");
                        yield new int[]{1};
                    }
                    default -> fallback(m);
                };
            });
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
    private static Object fallback(Method method) {
        if (method.getReturnType() == boolean.class) return false;
        if (method.getReturnType() == int.class) return 0;
        return null;
    }

    public static void main(String[] args) throws Exception {
        ChiTietDichVu ct = new ChiTietDichVu("Phụ tùng", "PT1", "Lọc dầu", "Cái", 2, 100);
        check(ct.getThanhTien() == 200, "Initial amount");
        ct.setSoLuong(3); ct.setDonGia(200);
        check(ct.getThanhTien() == 600, "Amount follows quantity and price");
        invalid(() -> ct.setSoLuong(0));
        invalid(() -> ct.setSoLuong(-1));
        invalid(() -> ct.setDonGia(-1));
        invalid(() -> ct.setDonGia(Long.MAX_VALUE));
        invalid(() -> new ChiTietDichVu("", "", "", "", 0, 100));
        check(ct.getSoLuong() == 3 && ct.getDonGia() == 200, "Rejected input retains old values");
        ct.setDonGia(0);
        check(ct.getThanhTien() == 0, "Free service accepted");

        List<ChiTietDichVu> source = new ArrayList<>(List.of(ct));
        DuLieuPhieuSuaChua d = new DuLieuPhieuSuaChua("TN1", "NV1", LocalDate.now(), source, List.of());
        source.clear(); ct.setSoLuong(10); d.getPhuTungs().clear();
        check(d.getPhuTungs().size() == 1 && d.getPhuTungs().get(0).getSoLuong() == 3, "Request isolates input list and objects");
        d.getPhuTungs().get(0).setSoLuong(20);
        check(d.getPhuTungs().get(0).getSoLuong() == 3, "Getter cannot mutate request");
        invalid(() -> new DuLieuPhieuSuaChua("TN1", "NV1", LocalDate.now(), List.of(), List.of()));
        invalid(() -> new DuLieuPhieuSuaChua.CongViec("", "", 1));
        invalid(() -> new DuLieuPhieuSuaChua.CongViec("Sửa xe", "", -1));

        String[] permissions = {"KH_SUA"};
        NhanVien nv = new NhanVien("NV_TEST", "Test", "test", "", "Nhân viên sửa chữa", NhanVien.DANG_LAM, permissions);
        permissions[0] = "PHAN_CONG";
        nv.getCacQuyen()[0] = "PHAN_CONG";
        check(!QuyenTruyCap.co(nv, QuyenTruyCap.Quyen.PHAN_CONG), "Permission array is encapsulated");
        NhanVien draft = new NhanVien(nv); draft.setHoTen("Changed"); draft.setVaiTro("Quản lý");
        check(nv.getHoTen().equals("Test") && !nv.getVaiTro().equals(draft.getVaiTro()), "Edit copy leaves employee unchanged");

        Constructor<KhoDuLieu> ctor = KhoDuLieu.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        KhoDuLieu cache = ctor.newInstance(); // Không gọi seed() hay SQL.
        ArrayList<NhanVien> staff = new ArrayList<>(List.of(nv));
        cache.setNhanViens(staff); staff.clear(); cache.getNhanViens().clear();
        check(cache.getNhanViens().size() == 1, "Cache protects list structure on input/output");

        Db db = new Db();
        SuaChuaService service = new SuaChuaService(new PhieuSuaChua_DAO(db.connection()));
        check(service.luu(nv, duLieu()).equals("SC_TEST"), "Returns persisted code");
        check(db.committed && !db.rolledBack && db.auto && db.batches == 2, "Atomic success includes both detail tables");
        check("TN_TEST".equals(db.header.get(5)) && "NV_TEST".equals(db.header.get(1)), "Retains receipt and technician links");
        for (int i = 0; i < 4; i++) {
            Db bad = new Db();
            bad.failWork = i == 0; bad.exported = i == 1; bad.missingReceipt = i == 2;
            if (i == 3) bad.assigned = "OTHER";
            try {
                new SuaChuaService(new PhieuSuaChua_DAO(bad.connection())).luu(nv, duLieu());
                throw new AssertionError("Should reject scenario " + i);
            } catch (SQLException expected) {
                check(bad.rolledBack && !bad.committed && bad.auto, "Rollback/restore scenario " + i);
                if (i == 1 || i == 3) check(bad.writes == 0, "No writes for exported/unassigned repair");
            }
        }
        Db created = new Db(); created.missingRepair = true;
        NhanVien manager = new NhanVien("QL", "Manager", "ql", "", "Quản lý");
        new SuaChuaService(new PhieuSuaChua_DAO(created.connection())).luu(manager, duLieu());
        check(created.committed && created.writes == 4, "Manager creates repair plus both details");
        Db denied = new Db();
        try {
            new SuaChuaService(new PhieuSuaChua_DAO(denied.connection())).luu(
                    new NhanVien("NV_TEST", "", "", "", "Nhân viên kho"), duLieu());
            throw new AssertionError("Warehouse must not save repair");
        } catch (IllegalArgumentException expected) { check(denied.writes == 0, "Permission check before SQL"); }
        Db otherAssignee = new Db();
        try {
            NhanVien other = new NhanVien(nv); other.setMaNV("OTHER");
            new SuaChuaService(new PhieuSuaChua_DAO(otherAssignee.connection())).luu(other, duLieu());
            throw new AssertionError("Technician must not reassign");
        } catch (IllegalArgumentException expected) { check(otherAssignee.writes == 0, "Reassignment rejected before SQL"); }
        Db transaction = new Db(); transaction.auto = false;
        try {
            new PhieuSuaChua_DAO(transaction.connection()).luu(duLieu(), null);
            throw new AssertionError("Must not commit caller transaction");
        } catch (SQLException expected) { check(!transaction.committed && !transaction.rolledBack && transaction.writes == 0, "Preserves caller transaction"); }

        JButton button = UI.BaoVeQuyen.ganQuyen(new JButton("Nhãn cũ"), QuyenTruyCap.Quyen.KH_XOA);
        button.setText("Nhãn mới"); button.setVisible(true);
        JPanel panel = new JPanel(); panel.add(button); UI.BaoVeQuyen.locNut(panel);
        check(!button.isVisible(), "No session: renamed button remains denied");
        System.out.println("PASS: " + checks + " OOP and transaction checks (no live database)");
    }
}
