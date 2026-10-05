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
        JPanel tim = new JPanel(new BorderLayout(8, 0));
        tim.setOpaque(false);
        tim.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JLabel lbl = new JLabel("Từ khóa (số phiếu / biển số / tình trạng):");
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
        for (PhieuBaoHanh p : KhoDuLieu.get().getPhieuBaoHanhs()) {
            String gop = (p.getSoPhieu() + " " + p.getBienSo() + " " + p.getTinhTrang()).toLowerCase();
            if (q.isEmpty() || gop.contains(q)) {
                model.addRow(new Object[]{p.getSoPhieu(), p.getBienSo(), p.getNgayBatDau(), p.getNgayKetThuc(),
                        p.getNguoiLap(), p.getTinhTrang(), p.getNoiDung()});
            }
        }
    }
}
