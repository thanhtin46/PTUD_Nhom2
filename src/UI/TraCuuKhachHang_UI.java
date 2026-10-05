package UI;

import Entity.KhachHang;
import Entity.KhoDuLieu;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TraCuuKhachHang_UI extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final DefaultTableModel model = UiHelper.model(
            "Mã KH", "Họ tên", "SĐT", "CCCD", "Địa chỉ", "Email", "Ghi chú", "Ngày ĐK");

    public TraCuuKhachHang_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("🔍 Tra cứu khách hàng"), BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Kết quả tra cứu");
        JPanel tim = new JPanel(new BorderLayout(8, 0));
        tim.setOpaque(false);
        tim.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JLabel lbl = new JLabel("Từ khóa (mã / tên / SĐT):");
        lbl.setFont(UiHelper.FONT_NHAN);
        lbl.setForeground(new Color(60, 70, 85));
        tim.add(lbl, BorderLayout.WEST);
        UiHelper.trangTriInput(txtTuKhoa);
        tim.add(txtTuKhoa, BorderLayout.CENTER);
        tim.add(UiHelper.nut("Tìm kiếm", new Color(37, 99, 168), e -> napBang()), BorderLayout.EAST);
        txtTuKhoa.addActionListener(e -> napBang());
        bangPanel.add(tim, BorderLayout.NORTH);

        JTable table = UiHelper.bang(model);
        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        root.add(bangPanel, BorderLayout.CENTER);
        napBang();
    }

    private void napBang() {
        String q = txtTuKhoa.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (KhachHang kh : KhoDuLieu.get().getKhachHangs()) {
            String gop = (kh.getMaKH() + " " + kh.getHoTen() + " " + kh.getSdt()).toLowerCase();
            if (q.isEmpty() || gop.contains(q)) {
                model.addRow(new Object[]{kh.getMaKH(), kh.getHoTen(), kh.getSdt(), kh.getCccd(),
                        kh.getDiaChi(), kh.getEmail(), kh.getGhiChu(), kh.getNgayDangKy()});
            }
        }
    }
}
