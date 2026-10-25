package UI;

import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TraCuuPhuongTien_UI extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final DefaultTableModel model = UiHelper.model(
            "Biển số", "Hãng", "Dòng xe", "Chủ xe", "SĐT", "Tiếp nhận");
    private final JTable table;

    public TraCuuPhuongTien_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("🔍 Tra cứu phương tiện"), BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Kết quả tra cứu");
        JPanel tim = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        tim.setOpaque(false);
        tim.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JLabel lbl = new JLabel("Từ khóa (biển số / hãng / chủ xe):");
        lbl.setFont(UiHelper.FONT_NHAN);
        lbl.setForeground(new Color(60, 70, 85));
        tim.add(lbl);
        txtTuKhoa.setPreferredSize(new Dimension(320, 36));
        UiHelper.trangTriInput(txtTuKhoa);
        tim.add(txtTuKhoa);
        tim.add(UiHelper.nut("Tìm kiếm", new Color(37, 99, 168), e -> napBang()));
        txtTuKhoa.addActionListener(e -> napBang());
        bangPanel.add(tim, BorderLayout.NORTH);

        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // Biển số
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Hãng
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Dòng xe
        table.getColumnModel().getColumn(3).setPreferredWidth(180); // Chủ xe
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // SĐT
        table.getColumnModel().getColumn(5).setPreferredWidth(150); // Tiếp nhận
        UiHelper.canGiua(table, 0, 4, 5);
        UiHelper.canTrai(table, 1, 2, 3);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    try {
						lapPhieuTiepNhan();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setOpaque(false);
        JButton btnTiepNhan = UiHelper.nut("Lập phiếu tiếp nhận cho xe đã chọn", UiIcon.of(UiIcon.FILE, 15), UiHelper.XANH_TRUNG, true, e -> {
			try {
				lapPhieuTiepNhan();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
        bottom.add(btnTiepNhan);
        bangPanel.add(bottom, BorderLayout.SOUTH);

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

    private void lapPhieuTiepNhan() throws Exception {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một xe trong bảng kết quả tra cứu để lập phiếu tiếp nhận!", "Chưa chọn xe", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String bienSo = model.getValueAt(dong, 0).toString().trim();
        PhuongTien xe = KhoDuLieu.get().timXeTheoBienSo(bienSo);
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
}
