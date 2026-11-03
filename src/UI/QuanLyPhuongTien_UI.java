package UI;

import DAO.PhuongTien_DAO;
import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Màn hình Quản lý thông tin phương tiện (Thêm, Sửa, Xóa, Làm mới) dành cho Tiếp tân và Quản lý:
 * - Biển số xe: Không được bỏ trống, bắt đầu 2 số, 1 chữ in hoa, dấu '-' và 5 số (VD: 51A-12345)
 * - Hãng xe: Combobox các hãng xe, chữ cái đầu mỗi từ viết hoa và các chữ sau viết thường, chỉ được nhập chữ
 * - Dòng xe / Model: Combobox đồng bộ theo hãng xe, chữ cái đầu mỗi từ viết hoa và các chữ sau viết thường, chỉ được nhập chữ
 * - Số khung (VIN): Theo chuẩn số khung thực tế ngoài đời (đúng 17 ký tự chữ và số, không chứa I, O, Q)
 * - Màu sơn: Bắt đầu bằng chữ cái, toàn bộ là chữ (không chứa số, ký tự đặc biệt)
 * - Năm sản xuất: Gồm 4 chữ số, bắt đầu bằng 19 hoặc 20
 * - Số km hiện tại (ODO): Chỉ được nhập số
 * - Cho phép nhập tất cả ký tự vào các ô nhập liệu, kiểm tra và hiển thị thông báo chi tiết khi nhấn Thêm/Sửa
 * - Nút Xóa hiển thị trực quan, hỗ trợ xóa an toàn cascade CSDL SQL Server
 */
public class QuanLyPhuongTien_UI extends JPanel {
    private static final String[] DS_HANG_XE = {
        "Toyota", "Honda", "Hyundai", "Kia", "Mazda", "Ford", "Mitsubishi", 
        "Vinfast", "Mercedes", "Audi", "Lexus", "Porsche", 
        "Volkswagen", "Volvo", "Land Rover", "Peugeot", "Suzuki", "Subaru", 
        "Isuzu", "Nissan", "Chevrolet", "Jeep", "Tesla", "Skoda", 
        "Haval", "Geely", "Chery", "Wuling", "Ferrari", "Lamborghini", 
        "Rolls Royce", "Bentley", "Aston Martin", "Maserati", "Jaguar", 
        "Bugatti", "Mclaren", "Genesis", "Infiniti", "Acura", "Cadillac", 
        "Lincoln", "Mini", "Alfa Romeo", "Chrysler", "Dodge", "Ram", 
        "Fiat", "Renault", "Citroen", "Seat", "Cupra", "Smart", "Lotus", 
        "Daihatsu", "Ssangyong"
    };

    private static final Map<String, String[]> MAP_DONG_XE = new LinkedHashMap<>();

    static {
        MAP_DONG_XE.put("Toyota", new String[]{"Vios", "Camry", "Corolla Altis", "Corolla Cross", "Fortuner", "Innova", "Innova Cross", "Hilux", "Raize", "Yaris Cross", "Veloz Cross", "Avanza Premio", "Land Cruiser", "Prado", "Alphard", "Wigo", "Yaris"});
        MAP_DONG_XE.put("Honda", new String[]{"City", "Civic", "Accord", "Brio"});
        MAP_DONG_XE.put("Hyundai", new String[]{"Accent", "Grand", "Elantra", "Creta", "Tucson", "Santa Fe", "Stargazer", "Custin", "Palisade", "Venue"});
        MAP_DONG_XE.put("Kia", new String[]{"Morning", "Soluto", "Sonet", "Seltos", "Carens", "Sportage", "Sorento", "Carnival"});
        MAP_DONG_XE.put("Mazda", new String[]{"Mazda Hai", "Mazda Ba", "Mazda Sau", "Triton"});
        MAP_DONG_XE.put("Ford", new String[]{"Ranger", "Ranger Raptor", "Everest", "Explorer", "Transit", "Territory"});
        MAP_DONG_XE.put("Mitsubishi", new String[]{"Xpander", "Xpander Cross", "Xforce", "Outlander", "Pajero Sport", "Triton", "Attrage"});
        MAP_DONG_XE.put("Vinfast", new String[]{"Fadil", "Lux", "President"});
        MAP_DONG_XE.put("Mercedes", new String[]{"Maybach", "Cla", "Gla", "Glb", "Glc", "Gle", "Gls"});
        MAP_DONG_XE.put("Audi", new String[]{"Sedan", "Sportback", "Coupe"});
        MAP_DONG_XE.put("Lexus", new String[]{"Sedan", "Suv", "Cross"});
        MAP_DONG_XE.put("Porsche", new String[]{"Macan", "Cayenne", "Panamera", "Taycan", "Cayman", "Boxster"});
        MAP_DONG_XE.put("Volkswagen", new String[]{"Teramont", "Tiguan", "Touareg", "Viloran", "Virtus", "Polo"});
        MAP_DONG_XE.put("Volvo", new String[]{"Cross Country", "Sedan"});
        MAP_DONG_XE.put("Land Rover", new String[]{"Range Rover", "Range Rover Sport", "Range Rover Velar", "Range Rover Evoque", "Defender", "Discovery"});
        MAP_DONG_XE.put("Peugeot", new String[]{"Traveller", "Sedan"});
        MAP_DONG_XE.put("Suzuki", new String[]{"Swift", "Ertiga", "Ciaz", "Jimny", "Carry"});
        MAP_DONG_XE.put("Subaru", new String[]{"Forester", "Outback"});
        MAP_DONG_XE.put("Nissan", new String[]{"Almera", "Navara", "Kicks", "Terra"});
        MAP_DONG_XE.put("Isuzu", new String[]{"Mu", "Truck"});
        MAP_DONG_XE.put("Chevrolet", new String[]{"Colorado", "Trailblazer", "Spark", "Cruze"});
        MAP_DONG_XE.put("Jeep", new String[]{"Wrangler", "Gladiator", "Grand Cherokee"});
        MAP_DONG_XE.put("Tesla", new String[]{"Cybertruck", "Roadster"});
        MAP_DONG_XE.put("Skoda", new String[]{"Kodiaq", "Karoq", "Octavia", "Superb"});
        MAP_DONG_XE.put("Haval", new String[]{"Jolion", "Suv"});
        MAP_DONG_XE.put("Chery", new String[]{"Omoda", "Tiggo"});
        MAP_DONG_XE.put("Wuling", new String[]{"Bingo", "Mini"});
        MAP_DONG_XE.put("Ferrari", new String[]{"Roma", "Portofino", "Purosangue"});
        MAP_DONG_XE.put("Lamborghini", new String[]{"Urus", "Revuelto", "Aventador"});
        MAP_DONG_XE.put("Rolls Royce", new String[]{"Phantom", "Ghost", "Cullinan", "Spectre"});
        MAP_DONG_XE.put("Bentley", new String[]{"Continental", "Flying Spur", "Bentayga"});
        MAP_DONG_XE.put("Aston Martin", new String[]{"Vantage", "Sedan"});
        MAP_DONG_XE.put("Maserati", new String[]{"Ghibli", "Levante", "Grecale", "Quattroporte"});
        MAP_DONG_XE.put("Jaguar", new String[]{"Pace", "Sedan"});
        MAP_DONG_XE.put("Bugatti", new String[]{"Chiron", "Divo", "Bolide", "Tourbillon"});
        MAP_DONG_XE.put("Mclaren", new String[]{"Artura", "Speed"});
        MAP_DONG_XE.put("Genesis", new String[]{"Sedan", "Suv"});
        MAP_DONG_XE.put("Cadillac", new String[]{"Escalade", "Sedan"});
        MAP_DONG_XE.put("Lincoln", new String[]{"Navigator", "Aviator", "Nautilus", "Corsair"});
        MAP_DONG_XE.put("Mini", new String[]{"Cooper", "Countryman", "Clubman"});
        MAP_DONG_XE.put("Alfa Romeo", new String[]{"Giulia", "Stelvio", "Tonale"});
        MAP_DONG_XE.put("Chrysler", new String[]{"Pacifica", "Sedan"});
        MAP_DONG_XE.put("Dodge", new String[]{"Charger", "Challenger", "Durango"});
        MAP_DONG_XE.put("Ram", new String[]{"Classic", "Truck"});
        MAP_DONG_XE.put("Fiat", new String[]{"Panda", "Tipo"});
        MAP_DONG_XE.put("Renault", new String[]{"Kaptur", "Duster", "Koleos", "Megane"});
        MAP_DONG_XE.put("Citroen", new String[]{"Aircross", "Sedan"});
        MAP_DONG_XE.put("Seat", new String[]{"Ibiza", "Leon", "Ateca", "Tarraco"});
        MAP_DONG_XE.put("Cupra", new String[]{"Formentor", "Leon", "Ateca", "Born"});
        MAP_DONG_XE.put("Lotus", new String[]{"Emira", "Eletre", "Evija"});
        MAP_DONG_XE.put("Daihatsu", new String[]{"Terios", "Xenia", "Rocky"});
        MAP_DONG_XE.put("Ssangyong", new String[]{"Tivoli", "Korando", "Rexton", "Musso"});
    }

    private JTextField txtBienSo, txtSoKhung, txtMauSon, txtNamSX, txtOdo;
    private JComboBox<String> cboHangXe;
    private JComboBox<String> cboDongXe;
    private JComboBox<String> cboChuXe;
    private JTable table;
    private DefaultTableModel model;

    private boolean dangCapNhatDongXe = false;

    private final PhuongTien_DAO phuongTienDAO = new PhuongTien_DAO();
    private final KhoDuLieu kho = KhoDuLieu.get();

    public QuanLyPhuongTien_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Quản lý phương tiện"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0.32);
        split.setDividerLocation(490);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin xe", 490);

        txtBienSo = new JTextField();
        cboHangXe = new JComboBox<>(DS_HANG_XE);
        cboHangXe.setEditable(true);

        cboDongXe = new JComboBox<>();
        cboDongXe.setEditable(true);

        txtSoKhung = new JTextField();
        txtMauSon = new JTextField();
        txtNamSX = new JTextField();
        txtOdo = new JTextField();

        cboChuXe = new JComboBox<>();
        napComboChuXe();

        // Đồng bộ danh sách dòng xe theo hãng xe ban đầu (Toyota)
        if (cboHangXe.getItemCount() > 0) {
            cboHangXe.setSelectedIndex(0);
            capNhatDanhSachDongXe(cboHangXe.getItemAt(0), null);
        }

        // Bắt sự kiện khi chọn hoặc thay đổi hãng xe thì tự động cập nhật danh sách dòng xe tương ứng
        cboHangXe.addActionListener(e -> {
            if (dangCapNhatDongXe) return;
            Object sel = cboHangXe.getSelectedItem();
            String hang = sel != null ? sel.toString().trim() : "";
            capNhatDanhSachDongXe(hang, null);
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Biển số xe *", txtBienSo);
        UiHelper.themDong(form, 1, "Hãng xe *", cboHangXe);
        UiHelper.themDong(form, 2, "Dòng xe / Model *", cboDongXe);
        UiHelper.themDong(form, 3, "Số khung (VIN) *", txtSoKhung);
        UiHelper.themDong(form, 4, "Màu sơn *", txtMauSon);
        UiHelper.themDong(form, 5, "Năm sản xuất *", txtNamSX);
        UiHelper.themDong(form, 6, "Chủ sở hữu *", cboChuXe);
        UiHelper.themDong(form, 7, "Số km hiện tại *", txtOdo);

        JPanel panelNut = new JPanel(new BorderLayout(0, 10));
        panelNut.setOpaque(false);
        panelNut.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Hàng nút chức năng Thêm, Sửa, Xóa, Làm mới hiển thị trực quan đầy đủ
        JPanel hangNut1 = new JPanel(new GridLayout(1, 4, 8, 0));
        hangNut1.setOpaque(false);
        hangNut1.add(UiHelper.nut("Thêm", UiHelper.XANH_LA, e -> them()));
        hangNut1.add(UiHelper.nut("Sửa", UiHelper.XANH_TRUNG, e -> sua()));
        hangNut1.add(UiHelper.nut("Xóa", UiHelper.CHU_CHAY, e -> xoa()));
        hangNut1.add(UiHelper.nut("Làm mới", UiHelper.XAM, e -> lamMoi()));

        JButton btnTiepNhan = UiHelper.nut("Lập phiếu tiếp nhận cho xe này", UiIcon.of(UiIcon.FILE, 16), UiHelper.XANH_TRUNG, true, e -> lapPhieuTiepNhan());
        BaoVeQuyen.ganQuyen(btnTiepNhan, Entity.QuyenTruyCap.Quyen.TIEP_NHAN);
        btnTiepNhan.setPreferredSize(new Dimension(btnTiepNhan.getPreferredSize().width, 38));

        panelNut.add(hangNut1, BorderLayout.NORTH);
        panelNut.add(btnTiepNhan, BorderLayout.CENTER);

        UiHelper.ganNoiDungForm(formPanel, form, panelNut);
        return formPanel;
    }

    private void napComboChuXe() {
        cboChuXe.removeAllItems();
        for (KhachHang kh : kho.getKhachHangs()) {
            cboChuXe.addItem(kh.getMaKH() + " - " + kh.getHoTen());
        }
    }

    private String maChuDangChon() {
        Object sel = cboChuXe.getSelectedItem();
        if (sel == null) return "";
        String s = sel.toString();
        int i = s.indexOf(" - ");
        return i > 0 ? s.substring(0, i) : s;
    }

    /**
     * Cập nhật danh sách dòng xe tương ứng với hãng xe được chọn
     */
    private void capNhatDanhSachDongXe(String hangXe, String dongChon) {
        if (dangCapNhatDongXe) return;
        dangCapNhatDongXe = true;
        try {
            cboDongXe.removeAllItems();
            String hangChuan = hangXe != null ? hangXe.trim() : "";
            String[] dsDong = null;
            for (Map.Entry<String, String[]> entry : MAP_DONG_XE.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(hangChuan)) {
                    dsDong = entry.getValue();
                    break;
                }
            }

            if (dsDong != null) {
                for (String d : dsDong) {
                    cboDongXe.addItem(d);
                }
            }

            if (dongChon != null && !dongChon.trim().isEmpty()) {
                String dc = dongChon.trim();
                boolean timThay = false;
                for (int i = 0; i < cboDongXe.getItemCount(); i++) {
                    if (cboDongXe.getItemAt(i).equalsIgnoreCase(dc)) {
                        cboDongXe.setSelectedIndex(i);
                        timThay = true;
                        break;
                    }
                }
                if (!timThay) {
                    cboDongXe.addItem(dc);
                    cboDongXe.setSelectedItem(dc);
                }
            } else if (cboDongXe.getItemCount() > 0) {
                cboDongXe.setSelectedIndex(0);
            }
        } finally {
            dangCapNhatDongXe = false;
        }
    }

    private void dongBoHangXe(String hangXe) {
        if (hangXe == null || hangXe.trim().isEmpty()) return;
        String hangChuan = hangXe.trim();
        for (int i = 0; i < cboHangXe.getItemCount(); i++) {
            if (cboHangXe.getItemAt(i).equalsIgnoreCase(hangChuan)) {
                cboHangXe.setSelectedIndex(i);
                return;
            }
        }
        cboHangXe.addItem(hangChuan);
        cboHangXe.setSelectedItem(hangChuan);
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách phương tiện");
        model = UiHelper.model("Biển số", "Hãng", "Dòng xe", "VIN", "Màu", "Năm", "ODO (km)",
                "Mã chủ", "Tên chủ", "SĐT", "Tiếp nhận gần nhất");
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Căn thẳng hàng giữa tiêu đề và nội dung cho từng cột
        dinhDangCot(0, SwingConstants.CENTER, 110); // Biển số
        dinhDangCot(1, SwingConstants.LEFT, 110);   // Hãng
        dinhDangCot(2, SwingConstants.LEFT, 130);   // Dòng xe
        dinhDangCot(3, SwingConstants.CENTER, 160); // VIN
        dinhDangCot(4, SwingConstants.CENTER, 85);  // Màu
        dinhDangCot(5, SwingConstants.CENTER, 75);  // Năm
        dinhDangCot(6, SwingConstants.RIGHT, 95);   // ODO (km)
        dinhDangCot(7, SwingConstants.CENTER, 85);  // Mã chủ
        dinhDangCot(8, SwingConstants.LEFT, 170);   // Tên chủ
        dinhDangCot(9, SwingConstants.CENTER, 115); // SĐT
        dinhDangCot(10, SwingConstants.CENTER, 140);// Tiếp nhận gần nhất

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienLenForm();
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    lapPhieuTiepNhan();
                }
            }
        });
        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        return bangPanel;
    }

    /** Căn lề cho tiêu đề và nội dung dữ liệu trong bảng */
    private void dinhDangCot(int cot, int canLe, int doRong) {
        if (cot < 0 || cot >= table.getColumnCount()) return;
        TableColumn tc = table.getColumnModel().getColumn(cot);
        tc.setPreferredWidth(doRong);

        final int padL = (canLe == SwingConstants.LEFT) ? 12 : 4;
        final int padR = (canLe == SwingConstants.RIGHT) ? 12 : 4;

        // Tiêu đề cột (Header)
        tc.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(canLe);
                lbl.setBackground(new Color(241, 245, 249));
                lbl.setForeground(new Color(30, 41, 59));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(8, padL, 8, padR)
                ));
                return lbl;
            }
        });

        // Nội dung dữ liệu (Cell)
        tc.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!t.isRowSelected(r)) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(new Color(30, 41, 59));
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(UiHelper.XANH_TRUNG);
                }
                if (comp instanceof JLabel lbl) {
                    lbl.setHorizontalAlignment(canLe);
                    lbl.setBorder(new EmptyBorder(0, padL, 0, padR));
                }
                return comp;
            }
        });
    }

    private void napBang() {
        model.setRowCount(0);

        // 1. Lấy dữ liệu trực tiếp từ SQL Server qua PhuongTien_DAO
        ArrayList<PhuongTien> dsXe = phuongTienDAO.layTatCaPhuongTien();
        if (dsXe.isEmpty()) {
            dsXe = kho.getPhuongTiens();
        }

        for (PhuongTien xe : dsXe) {
            KhachHang chu = xe.getKhachHang() != null ? xe.getKhachHang() : kho.timKhachHangTheoMa(xe.getMaChuXe());
            String ten = chu != null ? chu.getHoTen() : "";
            String sdt = chu != null ? chu.getSdt() : "";
            model.addRow(new Object[]{xe.getBienSo(), xe.getHangXe(), xe.getDongXe(), xe.getSoKhung(),
                    xe.getMauSon(), xe.getNamSX(), xe.getOdo(),
                    xe.getMaChuXe(), ten, sdt, xe.getNgayTiepNhanGanNhatHienThi()});
        }
    }

    private void them() {
        if (!kiemTraForm(true)) return;
        String maChu = maChuDangChon();
        if (maChu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chủ sở hữu cho xe!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboChuXe.requestFocus();
            return;
        }

        Object itemHang = cboHangXe.getSelectedItem();
        String hangXe = itemHang != null ? itemHang.toString().trim() : "";
        dongBoHangXe(hangXe);

        Object itemDong = cboDongXe.getSelectedItem();
        String dongXe = itemDong != null ? itemDong.toString().trim() : "";

        LocalDate ngayHomNay = LocalDate.now();
        PhuongTien xe = new PhuongTien(
                txtBienSo.getText().trim().toUpperCase(),
                hangXe,
                dongXe,
                txtSoKhung.getText().trim().toUpperCase(),
                "", // Bỏ nhập số máy
                txtMauSon.getText().trim(),
                txtNamSX.getText().trim(),
                txtOdo.getText().trim(),
                maChu,
                ngayHomNay);

        // Lưu vào SQL Server và đồng bộ toàn bộ kho dữ liệu
        boolean ok = kho.themPhuongTien(xe);

        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Thêm xe và lưu CSDL SQL Server thành công!" : "Thêm xe thành công (lưu bộ nhớ tạm)!");
    }

    private void xoa() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn xe cần xóa trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bienSo = model.getValueAt(dong, 0).toString().trim();

        // Kiểm tra ràng buộc tiếp nhận / sửa chữa qua PhuongTien_DAO
        boolean coHoatDong = phuongTienDAO.xeCoLichSuHoatDong(bienSo);
        String canhBao = coHoatDong
                ? "Xe [" + bienSo + "] đã có lịch sử phiếu tiếp nhận hoặc sửa chữa tại gara!\nBạn có chắc chắn muốn xóa toàn bộ thông tin xe này và các dữ liệu liên quan?"
                : "Bạn có chắc muốn xóa xe [" + bienSo + "] khỏi hệ thống?";

        if (JOptionPane.showConfirmDialog(this, canhBao,
                "Xác nhận xóa xe", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

        // Xóa an toàn qua PhuongTien_DAO
        boolean ok = phuongTienDAO.xoaPhuongTien(bienSo);
        if (ok) {
            kho.xoaPhuongTien(bienSo);
            napBang();
            lamMoi();
            JOptionPane.showMessageDialog(this, "Đã xóa xe [" + bienSo + "] thành công khỏi CSDL SQL Server!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể xóa xe [" + bienSo + "]! Vui lòng kiểm tra lại kết nối CSDL hoặc ràng buộc dữ liệu.", "Lỗi xóa xe", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn xe cần sửa trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!kiemTraForm(false)) return;

        String bienSo = model.getValueAt(dong, 0).toString().trim();
        PhuongTien xe = phuongTienDAO.timPhuongTienTheoBienSo(bienSo);
        if (xe == null) {
            xe = kho.timXeTheoBienSo(bienSo);
        }
        if (xe == null) return;

        Object itemHang = cboHangXe.getSelectedItem();
        String hangXe = itemHang != null ? itemHang.toString().trim() : "";
        dongBoHangXe(hangXe);

        Object itemDong = cboDongXe.getSelectedItem();
        String dongXe = itemDong != null ? itemDong.toString().trim() : "";

        xe.setHangXe(hangXe);
        xe.setDongXe(dongXe);
        xe.setSoKhung(txtSoKhung.getText().trim().toUpperCase());
        xe.setMauSon(txtMauSon.getText().trim());
        xe.setNamSX(txtNamSX.getText().trim());
        xe.setOdo(txtOdo.getText().trim());
        xe.setMaChuXe(maChuDangChon());

        // Cập nhật CSDL qua PhuongTien_DAO
        boolean ok = phuongTienDAO.capNhatPhuongTien(xe);
        kho.capNhatPhuongTien(xe);

        napBang();
        JOptionPane.showMessageDialog(this, ok ? "Cập nhật và đồng bộ CSDL SQL Server thành công!" : "Cập nhật thành công!");
    }

    private void lamMoi() {
        txtBienSo.setText("");
        if (cboHangXe.getItemCount() > 0) {
            cboHangXe.setSelectedIndex(0);
            capNhatDanhSachDongXe(cboHangXe.getItemAt(0), null);
        }
        txtSoKhung.setText("");
        txtMauSon.setText("");
        txtNamSX.setText("");
        txtOdo.setText("");
        napComboChuXe();
        if (cboChuXe.getItemCount() > 0) cboChuXe.setSelectedIndex(0);
        txtBienSo.setEditable(true);
        table.clearSelection();
        txtBienSo.requestFocus();
    }

    private void lapPhieuTiepNhan() {
        String bienSo = txtBienSo.getText().trim();
        if (bienSo.isEmpty()) {
            int dong = table.getSelectedRow();
            if (dong >= 0) {
                bienSo = model.getValueAt(dong, 0).toString().trim();
            }
        }
        if (bienSo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một xe trong danh sách để lập phiếu tiếp nhận!", "Chưa chọn xe", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PhuongTien xe = phuongTienDAO.timPhuongTienTheoBienSo(bienSo);
        if (xe == null) {
            xe = kho.timXeTheoBienSo(bienSo);
        }
        if (xe == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin xe với biển số: " + bienSo, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Main_UI mainUI = Main_UI.getInstance();
        if (mainUI == null) {
            mainUI = (Main_UI) SwingUtilities.getAncestorOfClass(Main_UI.class, this);
        }

        if (mainUI != null) {
            mainUI.chuyenTrangTiepNhan(xe);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy giao diện chính để chuyển trang.");
        }
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;
        txtBienSo.setText(model.getValueAt(dong, 0) != null ? model.getValueAt(dong, 0).toString() : "");
        String hang = model.getValueAt(dong, 1) != null ? model.getValueAt(dong, 1).toString().trim() : "";
        String dongXe = model.getValueAt(dong, 2) != null ? model.getValueAt(dong, 2).toString().trim() : "";
        dongBoHangXe(hang);
        capNhatDanhSachDongXe(hang, dongXe);

        txtSoKhung.setText(model.getValueAt(dong, 3) != null ? model.getValueAt(dong, 3).toString() : "");
        txtMauSon.setText(model.getValueAt(dong, 4) != null ? model.getValueAt(dong, 4).toString() : "");
        txtNamSX.setText(model.getValueAt(dong, 5) != null ? model.getValueAt(dong, 5).toString() : "");
        txtOdo.setText(model.getValueAt(dong, 6) != null ? model.getValueAt(dong, 6).toString() : "");

        String maChu = model.getValueAt(dong, 7) != null ? model.getValueAt(dong, 7).toString() : "";
        for (int i = 0; i < cboChuXe.getItemCount(); i++) {
            if (cboChuXe.getItemAt(i).startsWith(maChu + " - ")) {
                cboChuXe.setSelectedIndex(i);
                break;
            }
        }
        txtBienSo.setEditable(false);
    }

    private boolean kiemTraForm(boolean laThem) {
        String bienSo = txtBienSo.getText().trim().toUpperCase();
        txtBienSo.setText(bienSo);

        Object itemHang = cboHangXe.getSelectedItem();
        String hangXe = itemHang != null ? itemHang.toString().trim() : "";

        Object itemDong = cboDongXe.getSelectedItem();
        String dongXe = itemDong != null ? itemDong.toString().trim() : "";

        String soKhung = txtSoKhung.getText().trim().toUpperCase();
        txtSoKhung.setText(soKhung);

        String mauSon = txtMauSon.getText().trim();
        String namSX = txtNamSX.getText().trim();
        String odo = txtOdo.getText().trim();

        // 1. Biển số xe: Không được bỏ trống, bắt đầu 2 số, chữ in hoa, '-', 5 số
        if (bienSo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Biển số xe không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtBienSo.requestFocus();
            return false;
        }
        if (!bienSo.matches("^\\d{2}[A-Z]-\\d{5}$")) {
            JOptionPane.showMessageDialog(this, "Biển số xe không đúng định dạng!\nBiển số phải bắt đầu bằng 2 chữ số, tiếp theo là 1 chữ cái in hoa, dấu '-' và 5 chữ số phía sau (Ví dụ: 51A-12345, 29B-98765).", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtBienSo.requestFocus();
            return false;
        }

        // 2. Hãng xe: Không được để trống, chữ cái đầu mỗi từ viết hoa, các chữ cái sau viết thường, chỉ được nhập chữ
        if (hangXe.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãng xe không được để trống! Vui lòng chọn hoặc nhập hãng xe.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboHangXe.requestFocus();
            return false;
        }
        if (hangXe.matches(".*\\d.*")) {
            JOptionPane.showMessageDialog(this, "Hãng xe chỉ được nhập chữ, không được chứa chữ số!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboHangXe.requestFocus();
            return false;
        }
        if (!hangXe.matches("^[\\p{Lu}][\\p{Ll}]+( [\\p{Lu}][\\p{Ll}]+)*$")) {
            JOptionPane.showMessageDialog(this, "Hãng xe không hợp lệ!\nChữ cái đầu mỗi từ phải viết hoa, các chữ cái sau viết thường và chỉ được chứa chữ (Ví dụ: Toyota, Honda, Land Rover)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboHangXe.requestFocus();
            return false;
        }

        // 3. Dòng xe / Model: Không được để trống, chữ cái đầu mỗi từ viết hoa, các chữ cái sau viết thường, chỉ được nhập chữ
        if (dongXe.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dòng xe / Model không được để trống! Vui lòng chọn hoặc nhập dòng xe.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboDongXe.requestFocus();
            return false;
        }
        if (dongXe.matches(".*\\d.*")) {
            JOptionPane.showMessageDialog(this, "Dòng xe / Model chỉ được nhập chữ, không được chứa chữ số!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboDongXe.requestFocus();
            return false;
        }
        if (!dongXe.matches("^[\\p{Lu}][\\p{Ll}]+( [\\p{Lu}][\\p{Ll}]+)*$")) {
            JOptionPane.showMessageDialog(this, "Dòng xe / Model không hợp lệ!\nChữ cái đầu mỗi từ phải viết hoa, các chữ cái sau viết thường và chỉ được chứa chữ (Ví dụ: Vios, Camry, Corolla Cross)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboDongXe.requestFocus();
            return false;
        }

        // 4. Số khung (VIN): Theo chuẩn số khung thực tế ngoài đời (17 ký tự chữ và số, loại trừ I, O, Q)
        if (soKhung.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số khung (VIN) không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtSoKhung.requestFocus();
            return false;
        }
        if (!soKhung.matches("^[A-HJ-NPR-Z0-9]{17}$")) {
            JOptionPane.showMessageDialog(this, "Số khung (VIN) phải gồm đúng 17 ký tự chữ và số (không bao gồm chữ I, O, Q để tránh nhầm với số 0 và 1).", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtSoKhung.requestFocus();
            return false;
        }

        // 5. Màu sơn: Chữ cái đầu là chữ, và toàn bộ là chữ
        if (mauSon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Màu sơn không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtMauSon.requestFocus();
            return false;
        }
        if (!mauSon.matches("^[\\p{L}][\\p{L}\\s]*$")) {
            JOptionPane.showMessageDialog(this, "Màu sơn phải bắt đầu bằng chữ cái in hoa và chỉ chứa các chữ cái ", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtMauSon.requestFocus();
            return false;
        }

        // 6. Năm sản xuất: Bắt đầu từ 19 hoặc 20
        if (namSX.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Năm sản xuất không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtNamSX.requestFocus();
            return false;
        }
        int namHienTai = LocalDate.now().getYear();
        if (!namSX.matches("^(19|20)\\d{2}$") || Integer.parseInt(namSX) < 1900 || Integer.parseInt(namSX) > namHienTai + 1) {
            JOptionPane.showMessageDialog(this, "Năm sản xuất phải gồm 4 chữ số và bắt đầu bằng 19 hoặc 20 (từ 1900 đến " + (namHienTai + 1) + ").", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtNamSX.requestFocus();
            return false;
        }

        // 7. Số km hiện tại: Chỉ được số
        if (odo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số km hiện tại không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtOdo.requestFocus();
            return false;
        }
        if (!odo.matches("^\\d+$")) {
            JOptionPane.showMessageDialog(this, "Số km hiện tại chỉ được nhập các chữ số.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtOdo.requestFocus();
            return false;
        }

        // 8. Kiểm tra trùng biển số xe qua PhuongTien_DAO
        if (laThem && phuongTienDAO.kiemTraTrungBienSo(bienSo)) {
            JOptionPane.showMessageDialog(this, "Biển số xe \"" + bienSo + "\" đã tồn tại trong hệ thống!", "Cảnh báo trùng biển số", JOptionPane.WARNING_MESSAGE);
            txtBienSo.requestFocus();
            return false;
        }

        // 9. Kiểm tra trùng số VIN qua PhuongTien_DAO
        if (phuongTienDAO.kiemTraTrungVIN(soKhung, laThem ? null : bienSo)) {
            JOptionPane.showMessageDialog(this, "Số khung (VIN) \"" + soKhung + "\" đã được sử dụng cho xe khác trong hệ thống!", "Cảnh báo trùng số khung", JOptionPane.WARNING_MESSAGE);
            txtSoKhung.requestFocus();
            return false;
        }

        return true;
    }
}
