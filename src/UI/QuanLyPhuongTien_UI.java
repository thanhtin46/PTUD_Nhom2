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

public class QuanLyPhuongTien_UI extends JPanel {
    private JTextField txtBienSo, txtHangXe, txtDongXe, txtSoKhung, txtMauSon, txtNamSX, txtOdo;
    private JComboBox<String> cboChuXe;
    private JTable table;
    private DefaultTableModel model;

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
        txtHangXe = new JTextField();
        txtDongXe = new JTextField();
        txtSoKhung = new JTextField();
        txtMauSon = new JTextField();
        txtNamSX = new JTextField();
        txtOdo = new JTextField();

        cboChuXe = new JComboBox<>();
        napComboChuXe();

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Biển số xe *", txtBienSo);
        UiHelper.themDong(form, 1, "Hãng xe *", txtHangXe);
        UiHelper.themDong(form, 2, "Dòng xe / Model *", txtDongXe);
        UiHelper.themDong(form, 3, "Số khung (VIN) *", txtSoKhung);
        UiHelper.themDong(form, 4, "Màu sơn", txtMauSon);
        UiHelper.themDong(form, 5, "Năm sản xuất", txtNamSX);
        UiHelper.themDong(form, 6, "Chủ sở hữu *", cboChuXe);
        UiHelper.themDong(form, 7, "Số km hiện tại", txtOdo);

        JPanel panelNut = new JPanel(new BorderLayout(0, 10));
        panelNut.setOpaque(false);
        panelNut.setBorder(new EmptyBorder(16, 0, 0, 0));

        JPanel hangNut1 = new JPanel(new GridLayout(1, 4, 8, 0));
        hangNut1.setOpaque(false);
        hangNut1.add(UiHelper.nut("Thêm", UiHelper.XANH_LA, e -> them()));
        hangNut1.add(UiHelper.nut("Sửa", UiHelper.XANH_TRUNG, e -> sua()));
        hangNut1.add(UiHelper.nut("Xóa", UiHelper.CHU_CHAY, e -> xoa()));
        hangNut1.add(UiHelper.nut("Làm mới", UiHelper.XAM, e -> lamMoi()));

        JButton btnTiepNhan = UiHelper.nut("Lập phiếu tiếp nhận cho xe này", UiIcon.of(UiIcon.FILE, 16), UiHelper.XANH_TRUNG, true, e -> lapPhieuTiepNhan());
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
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.XE_SUA)) return;
        if (!kiemTraForm(true)) return;
        String maChu = maChuDangChon();
        if (maChu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chủ sở hữu!");
            return;
        }

        LocalDate ngayHomNay = LocalDate.now();
        PhuongTien xe = new PhuongTien(
                txtBienSo.getText().trim(),
                txtHangXe.getText().trim(),
                txtDongXe.getText().trim(),
                txtSoKhung.getText().trim(),
                "", // Bỏ nhập số máy
                txtMauSon.getText().trim(),
                txtNamSX.getText().trim(),
                txtOdo.getText().trim(),
                maChu,
                ngayHomNay);

        // Lưu trực tiếp vào SQL Server qua PhuongTien_DAO và cập nhật cache
        boolean ok = phuongTienDAO.themPhuongTien(xe);
        kho.themPhuongTien(xe);

        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Thêm xe và lưu CSDL SQL Server thành công!" : "Thêm xe thành công (lưu bộ nhớ tạm)!");
    }

    private void xoa() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.XE_XOA)) return;
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn xe cần xóa!");
            return;
        }

        String bienSo = model.getValueAt(dong, 0).toString().trim();

        // Kiểm tra ràng buộc tiếp nhận / sửa chữa qua PhuongTien_DAO
        boolean coHoatDong = phuongTienDAO.xeCoLichSuHoatDong(bienSo);
        String canhBao = coHoatDong
                ? "Xe [" + bienSo + "] đã có lịch sử phiếu tiếp nhận hoặc sửa chữa tại gara!\nBạn có chắc chắn muốn xóa toàn bộ thông tin xe này?"
                : "Bạn có chắc muốn xóa xe [" + bienSo + "] khỏi hệ thống?";

        if (JOptionPane.showConfirmDialog(this, canhBao,
                "Xác nhận xóa xe", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

        // Xóa an toàn qua PhuongTien_DAO
        boolean ok = phuongTienDAO.xoaPhuongTien(bienSo);
        kho.xoaPhuongTien(bienSo);

        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Đã xóa xe khỏi CSDL SQL Server!" : "Đã xóa xe!");
    }

    private void sua() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.XE_SUA)) return;
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn xe cần sửa!");
            return;
        }
        if (!kiemTraForm(false)) return;

        String bienSo = model.getValueAt(dong, 0).toString().trim();
        PhuongTien xe = phuongTienDAO.timPhuongTienTheoBienSo(bienSo);
        if (xe == null) {
            xe = kho.timXeTheoBienSo(bienSo);
        }
        if (xe == null) return;

        xe.setHangXe(txtHangXe.getText().trim());
        xe.setDongXe(txtDongXe.getText().trim());
        xe.setSoKhung(txtSoKhung.getText().trim());
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
        txtHangXe.setText("");
        txtDongXe.setText("");
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
        txtHangXe.setText(model.getValueAt(dong, 1) != null ? model.getValueAt(dong, 1).toString() : "");
        txtDongXe.setText(model.getValueAt(dong, 2) != null ? model.getValueAt(dong, 2).toString() : "");
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
        String bienSo = txtBienSo.getText().trim();
        String hangXe = txtHangXe.getText().trim();
        String dongXe = txtDongXe.getText().trim();
        String soKhung = txtSoKhung.getText().trim();
        String namSX = txtNamSX.getText().trim();
        String odo = txtOdo.getText().trim();

        if (bienSo.isEmpty() || soKhung.isEmpty()) {
            UiHelper.loi(this, "Biển số xe và Số khung không được để trống!", txtBienSo);
            return false;
        }
        if (hangXe.isEmpty()) {
            UiHelper.loi(this, "Vui lòng nhập Hãng xe!", txtHangXe);
            return false;
        }
        if (dongXe.isEmpty()) {
            UiHelper.loi(this, "Vui lòng nhập Dòng xe / Model!", txtDongXe);
            return false;
        }
        if (!namSX.isEmpty()) {
            int namMax = LocalDate.now().getYear() + 1;
            if (!namSX.matches("\\d{4}") || Integer.parseInt(namSX) < 1950 || Integer.parseInt(namSX) > namMax) {
                UiHelper.loi(this, "Năm sản xuất phải gồm 4 chữ số, từ 1950 đến " + namMax + "!", txtNamSX);
                return false;
            }
        }
        if (!odo.isEmpty() && !odo.matches("\\d+")) {
            UiHelper.loi(this, "Số km hiện tại chỉ được nhập số!", txtOdo);
            return false;
        }

        // Kiểm tra trùng biển số xe qua PhuongTien_DAO
        if (laThem && phuongTienDAO.kiemTraTrungBienSo(bienSo)) {
            UiHelper.loi(this, "Biển số \"" + bienSo + "\" đã tồn tại trong hệ thống!", txtBienSo);
            return false;
        }

        // Kiểm tra trùng số VIN qua PhuongTien_DAO
        if (phuongTienDAO.kiemTraTrungVIN(soKhung, laThem ? null : bienSo)) {
            UiHelper.loi(this, "Số khung (VIN) \"" + soKhung + "\" đã được sử dụng cho xe khác!", txtSoKhung);
            return false;
        }

        return true;
    }
}
