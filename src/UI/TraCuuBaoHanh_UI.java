package UI;

import Entity.KhoDuLieu;
import Entity.PhieuBaoHanh;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TraCuuBaoHanh_UI extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final DefaultTableModel model = UiHelper.model(
            "Số phiếu", "Biển số", "Ngày BĐ", "Ngày KT", "Người lập", "Tình trạng", "Nội dung");

    public TraCuuBaoHanh_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("🔍 Tra cứu bảo hành"), BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Kết quả tra cứu");
        JPanel tim = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        tim.setOpaque(false);
        tim.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JLabel lbl = new JLabel("Từ khóa (số phiếu / biển số / tình trạng):");
        lbl.setFont(UiHelper.FONT_NHAN);
        lbl.setForeground(new Color(60, 70, 85));
        tim.add(lbl);
        txtTuKhoa.setPreferredSize(new Dimension(320, 36));
        UiHelper.trangTriInput(txtTuKhoa);
        tim.add(txtTuKhoa);
        tim.add(UiHelper.nut("Tìm kiếm", new Color(37, 99, 168), e -> napBang()));
        txtTuKhoa.addActionListener(e -> napBang());
        bangPanel.add(tim, BorderLayout.NORTH);

        JTable table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);   // Số phiếu
        table.getColumnModel().getColumn(1).setPreferredWidth(110);  // Biển số
        table.getColumnModel().getColumn(2).setPreferredWidth(110);  // Ngày BĐ
        table.getColumnModel().getColumn(3).setPreferredWidth(110);  // Ngày KT
        table.getColumnModel().getColumn(4).setPreferredWidth(160);  // Người lập
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // Tình trạng
        table.getColumnModel().getColumn(6).setPreferredWidth(250);  // Nội dung

        UiHelper.canGiua(table, 0, 1, 2, 3, 5);
        UiHelper.canTrai(table, 4, 6);

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        root.add(bangPanel, BorderLayout.CENTER);
        napBang();
    }

    private void napBang() {
        String q = txtTuKhoa.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (PhieuBaoHanh p : KhoDuLieu.get().getPhieuBaoHanhs()) {
            String gop = (p.getSoPhieu() + " " + p.getBienSo() + " " + p.getTinhTrang()).toLowerCase();
            if (q.isEmpty() || gop.contains(q)) {
                model.addRow(new Object[]{p.getSoPhieu(), p.getBienSo(), p.getNgayBatDau(), p.getNgayKetThuc(),
                        p.getNguoiLap(), p.getTinhTrang(), p.getNoiDung()});
            }
        }
    }
}
