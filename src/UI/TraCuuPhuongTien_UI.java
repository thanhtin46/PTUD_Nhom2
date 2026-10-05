package UI;

import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TraCuuPhuongTien_UI extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final DefaultTableModel model = UiHelper.model(
            "Biển số", "Hãng", "Dòng xe", "Chủ xe", "SĐT", "Tiếp nhận");

    public TraCuuPhuongTien_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("🔍 Tra cứu phương tiện"), BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Kết quả tra cứu");
        JPanel tim = new JPanel(new BorderLayout(8, 0));
        tim.setOpaque(false);
        tim.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JLabel lbl = new JLabel("Từ khóa (biển số / hãng / chủ xe):");
        lbl.setFont(UiHelper.FONT_NHAN);
        lbl.setForeground(new Color(60, 70, 85));
        tim.add(lbl, BorderLayout.WEST);
        UiHelper.trangTriInput(txtTuKhoa);
        tim.add(txtTuKhoa, BorderLayout.CENTER);
        tim.add(UiHelper.nut("Tìm kiếm", new Color(37, 99, 168), e -> napBang()), BorderLayout.EAST);
        txtTuKhoa.addActionListener(e -> napBang());
        bangPanel.add(tim, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(UiHelper.bang(model));
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        root.add(bangPanel, BorderLayout.CENTER);
        napBang();
    }

    private void napBang() {
        String q = txtTuKhoa.getText().trim().toLowerCase();
        model.setRowCount(0);
        KhoDuLieu kho = KhoDuLieu.get();
        for (PhuongTien xe : kho.getPhuongTiens()) {
            KhachHang chu = kho.timKhachHangTheoMa(xe.getMaChuXe());
            String ten = chu != null ? chu.getHoTen() : "";
            String sdt = chu != null ? chu.getSdt() : "";
            String gop = (xe.getBienSo() + " " + xe.getHangXe() + " " + xe.getDongXe() + " " + ten).toLowerCase();
            if (q.isEmpty() || gop.contains(q)) {
                model.addRow(new Object[]{xe.getBienSo(), xe.getHangXe(), xe.getDongXe(), ten, sdt, xe.getNgayTiepNhanGanNhat()});
            }
        }
    }
}
