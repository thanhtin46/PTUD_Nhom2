package tests;

import ConnectDB.ConnectDB;
import Entity.NhanVien;
import UI.DieuHanhSuaChua_UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestInteractiveFiltering {

    public static void main(String[] args) {
        System.out.println("========== BẮT ĐẦU KIỂM THỬ: TƯƠNG TÁC LỌC & TÌM KIẾM ==========");

        ConnectDB.getInstance().connect();
        NhanVien quanLy = new NhanVien("NV04", "Quản Lý Tổng", "quanLy", "123", "Quản lý");

        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Kiểm thử Điều hành sửa chữa - Tương tác lọc");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(1280, 800);

                DieuHanhSuaChua_UI ui = new DieuHanhSuaChua_UI(quanLy, null);
                frame.setContentPane(ui);
                frame.setVisible(true);

                // Chờ SwingWorker nạp dữ liệu xong
                Timer t = new Timer(1500, e1 -> {
                    try {
                        // 1. Kiểm tra lọc KTV: Chọn KTV NV101 trong bảng tableKtv
                        JTable tblKtv = findComponent(ui, JTable.class, 0); // bảng KTV
                        JTable tblPhieu = findComponent(ui, JTable.class, 1); // bảng phiếu

                        System.out.println("✔ Bảng KTV có: " + tblKtv.getRowCount() + " dòng.");
                        System.out.println("✔ Bảng Phiếu có: " + tblPhieu.getRowCount() + " dòng.");

                        // Tìm dòng KTV NV101
                        int rowNV101 = -1;
                        for (int i = 0; i < tblKtv.getRowCount(); i++) {
                            if ("NV101".equals(tblKtv.getValueAt(i, 0))) {
                                rowNV101 = i;
                                break;
                            }
                        }

                        if (rowNV101 >= 0) {
                            tblKtv.setRowSelectionInterval(rowNV101, rowNV101);
                            System.out.println("✔ Đã click chọn KTV NV101 ở dòng: " + rowNV101);
                        }

                        // Chờ cập nhật bảng phiếu bên phải
                        Timer t2 = new Timer(500, e2 -> {
                            try {
                                System.out.println("✔ Sau khi lọc KTV NV101, Bảng Phiếu còn: " + tblPhieu.getRowCount() + " phiếu.");
                                for (int i = 0; i < tblPhieu.getRowCount(); i++) {
                                    System.out.println("   + Phiếu: " + tblPhieu.getValueAt(i, 0)
                                            + " | KTV: " + tblPhieu.getValueAt(i, 3)
                                            + " | Hạn: " + tblPhieu.getValueAt(i, 4)
                                            + " | Tình trạng: " + tblPhieu.getValueAt(i, 6));
                                }

                                BufferedImage img = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_RGB);
                                Graphics2D g2 = img.createGraphics();
                                frame.paint(g2);
                                g2.dispose();

                                String artifactDir = new File("output").getAbsolutePath();
                        new File(artifactDir).mkdirs();
                                File out = new File(artifactDir, "test_dieu_hanh_loc_ktv.png");
                                ImageIO.write(img, "PNG", out);
                                System.out.println("✔ Đã lưu ảnh chụp giao diện lọc theo KTV: " + out.getAbsolutePath());

                                frame.dispose();
                                System.out.println("========== KIỂM THỬ TƯƠNG TÁC THÀNH CÔNG ==========");
                                System.exit(0);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                System.exit(1);
                            }
                        });
                        t2.setRepeats(false);
                        t2.start();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                });
                t.setRepeats(false);
                t.start();

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Component> T findComponent(Container parent, Class<T> clazz, int index) {
        int[] count = new int[]{0};
        return findComponentRecursive(parent, clazz, index, count);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Component> T findComponentRecursive(Container parent, Class<T> clazz, int targetIndex, int[] count) {
        for (Component c : parent.getComponents()) {
            if (clazz.isInstance(c)) {
                if (count[0] == targetIndex) {
                    return (T) c;
                }
                count[0]++;
            }
            if (c instanceof Container) {
                T res = findComponentRecursive((Container) c, clazz, targetIndex, count);
                if (res != null) return res;
            }
        }
        return null;
    }
}
