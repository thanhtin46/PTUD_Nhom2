package tests;

import ConnectDB.ConnectDB;
import DAO.DieuHanhSuaChua_DAO;
import Entity.NhanVien;
import Entity.PhieuDieuHanhDTO;
import Entity.ThongKeKtvDTO;
import UI.DieuHanhSuaChua_UI;
import UI.Main_UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

public class TestDieuHanhSuaChua {

    public static void main(String[] args) {
        System.out.println("========== BẮT ĐẦU KIỂM THỬ: ĐIỀU HÀNH SỬA CHỮA ==========");

        // 1. Kiểm tra kết nối CSDL
        ConnectDB.getInstance().connect();
        if (ConnectDB.getInstance().getConnection() == null) {
            System.err.println("❌ LỖI: Không thể kết nối cơ sở dữ liệu!");
            System.exit(1);
        }
        System.out.println("✔ Kết nối cơ sở dữ liệu SQL Server thành công.");

        DieuHanhSuaChua_DAO dao = new DieuHanhSuaChua_DAO();

        // 2. Kiểm thử DAO: layDanhSachTheoDoi
        try {
            ArrayList<PhieuDieuHanhDTO> dsPhieu = dao.layDanhSachTheoDoi();
            System.out.println("✔ layDanhSachTheoDoi() trả về: " + dsPhieu.size() + " phiếu.");

            int demChuaHT = 0;
            int demHenHomNay = 0;
            int demQuaHen = 0;
            boolean coChuaCoNgayHen = false;
            boolean coQuaHen1Ngay = false;

            for (PhieuDieuHanhDTO p : dsPhieu) {
                if (p.isChuaHoanThanh()) {
                    demChuaHT++;
                    if (p.isQuaHen()) {
                        demQuaHen++;
                        if (p.getSoNgayQuaHen() == 1) {
                            coQuaHen1Ngay = true;
                            System.out.println("  -> [TEST CASE 1] Phiếu quá hẹn 1 ngày: " + p.getMaPSC()
                                    + " (Hạn: " + p.getNgayHenHienThi() + ", KTV: " + p.getTenKTV() + ")");
                        }
                    } else if (p.isHenHomNay()) {
                        demHenHomNay++;
                        System.out.println("  -> [TEST CASE 2] Phiếu hẹn hôm nay (không quá hẹn): " + p.getMaPSC()
                                + " (Hạn: " + p.getNgayHenHienThi() + ", KTV: " + p.getTenKTV() + ")");
                    }
                    if (p.getNgayHoanThanhDuKien() == null) {
                        coChuaCoNgayHen = true;
                        System.out.println("  -> [TEST CASE 4] Phiếu chưa có ngày hẹn: " + p.getMaPSC()
                                + " hiển thị: \"" + p.getNgayHenHienThi() + "\"");
                    }
                }
            }

            System.out.println("  => Tổng chưa hoàn thành: " + demChuaHT + ", Hẹn hôm nay: " + demHenHomNay + ", Quá hẹn: " + demQuaHen);
            if (!coQuaHen1Ngay) System.err.println("⚠ Cảnh báo: Chưa có phiếu quá hẹn đúng 1 ngày.");
            if (!coChuaCoNgayHen) System.err.println("⚠ Cảnh báo: Chưa có phiếu không có ngày hẹn.");

            // 3. Kiểm thử DAO: layThongKeKyThuatVien
            ArrayList<ThongKeKtvDTO> dsKtv = dao.layThongKeKyThuatVien();
            System.out.println("✔ layThongKeKyThuatVien() trả về: " + dsKtv.size() + " KTV.");

            boolean coKtvKhongCoPhieu = false;
            for (ThongKeKtvDTO k : dsKtv) {
                System.out.println("  -> KTV: " + k.getMaKTV() + " - " + k.getTenKTV()
                        + " | Chức vụ: " + k.getChucVu()
                        + " | Đang phụ trách: " + k.getSoPhieuDangPhuTrach()
                        + " | Quá hẹn: " + k.getSoPhieuQuaHen());

                if (k.getSoPhieuDangPhuTrach() == 0 && k.getSoPhieuQuaHen() == 0) {
                    coKtvKhongCoPhieu = true;
                    System.out.println("  -> [TEST CASE 5] KTV không có phiếu (số lượng 0): " + k.getMaKTV() + " - " + k.getTenKTV());
                }
            }
            if (!coKtvKhongCoPhieu) {
                System.err.println("⚠ Cảnh báo: Chưa tìm thấy KTV có 0 phiếu trong danh sách.");
            }

        } catch (Exception ex) {
            System.err.println("❌ LỖI TRUY VẤN DAO: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        // 4. Kiểm thử giao diện Swing & Chụp ảnh màn hình
        SwingUtilities.invokeLater(() -> {
            try {
                // Tạo nhân viên Quản lý
                NhanVien quanLy = new NhanVien("NV04", "Quản Lý Tổng", "quanLy", "123", "Quản lý");

                JFrame frame = new JFrame("Kiểm thử Điều hành sửa chữa");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(1280, 800);

                DieuHanhSuaChua_UI ui = new DieuHanhSuaChua_UI(quanLy, null);
                frame.setContentPane(ui);
                frame.setVisible(true);

                // Chờ giao diện nạp SwingWorker xong
                Timer timer = new Timer(1500, e -> {
                    try {
                        BufferedImage image = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = image.createGraphics();
                        frame.paint(g2);
                        g2.dispose();

                        String artifactDir = new File("output").getAbsolutePath();
                        new File(artifactDir).mkdirs();
                        File out1 = new File(artifactDir, "test_dieu_hanh_sua_chua.png");
                        ImageIO.write(image, "PNG", out1);
                        System.out.println("✔ Đã lưu ảnh chụp giao diện: " + out1.getAbsolutePath());

                        frame.dispose();
                        System.out.println("========== HOÀN THÀNH TOÀN BỘ KIỂM THỬ THÀNH CÔNG ==========");
                        System.exit(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                });
                timer.setRepeats(false);
                timer.start();

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }
}
