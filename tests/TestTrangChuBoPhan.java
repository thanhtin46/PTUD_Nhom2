package tests;

import ConnectDB.ConnectDB;
import Entity.NhanVien;
import UI.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class TestTrangChuBoPhan {

    public static void main(String[] args) {
        System.out.println("========== BẮT ĐẦU KIỂM THỬ TRANG CHỦ CÁC BỘ PHẬN ==========");

        ConnectDB.getInstance().connect();

        NhanVien quanLy = new NhanVien("NV04", "Quản Lý Tổng", "quanLy", "123", "Quản lý");
        NhanVien tiepTan = new NhanVien("NV01", "Nguyễn Văn Tiếp Tân", "tiepTan", "123", "Nhân viên tiếp tân");
        NhanVien suaChua = new NhanVien("NV02", "Trần Văn Sửa Chữa", "suaChua", "123", "Nhân viên sửa chữa");
        NhanVien kho = new NhanVien("NV03", "Nguyễn Văn Kho", "kho", "123", "Nhân viên kho");
        NhanVien thuNgan = new NhanVien("NV05", "Trần Thị Thu Ngân", "thuNgan", "123", "Nhân viên thu ngân");

        SwingUtilities.invokeLater(() -> {
            try {
                String artifactDir = new File("output").getAbsolutePath();
                        new File(artifactDir).mkdirs();

                // 1. Kiểm thử TrangChuSuaChua_UI
                chupAnhTrangChu(new TrangChuSuaChua_UI(suaChua, null), "test_trangchu_suachua.png", artifactDir, () -> {
                    System.out.println("✔ [1/4] Đã chụp ảnh TrangChuSuaChua_UI (Kỹ thuật viên)");

                    // 2. Kiểm thử TrangChuKho_UI
                    chupAnhTrangChu(new TrangChuKho_UI(kho, null), "test_trangchu_kho.png", artifactDir, () -> {
                        System.out.println("✔ [2/4] Đã chụp ảnh TrangChuKho_UI (Thủ kho)");

                        // 3. Kiểm thử TrangChuThuNgan_UI
                        chupAnhTrangChu(new TrangChuThuNgan_UI(thuNgan, null), "test_trangchu_thungan.png", artifactDir, () -> {
                            System.out.println("✔ [3/4] Đã chụp ảnh TrangChuThuNgan_UI (Thu ngân)");

                            // 4. Kiểm thử TrangChuTiepTan_UI
                            chupAnhTrangChu(new TrangChuTiepTan_UI(tiepTan), "test_trangchu_tieptan.png", artifactDir, () -> {
                                System.out.println("✔ [4/4] Đã chụp ảnh TrangChuTiepTan_UI (Tiếp tân)");

                                System.out.println("========== TOÀN BỘ KIỂM THỬ TRANG CHỦ BỘ PHẬN THÀNH CÔNG ==========");
                                System.exit(0);
                            });
                        });
                    });
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void chupAnhTrangChu(JComponent ui, String fileName, String dir, Runnable onDone) {
        JFrame frame = new JFrame("Kiểm thử " + fileName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1360, 850);
        frame.setContentPane(ui);
        frame.setVisible(true);

        Timer t = new Timer(1800, e -> {
            try {
                BufferedImage img = new BufferedImage(1360, 850, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                frame.paint(g);
                g.dispose();

                File out = new File(dir, fileName);
                ImageIO.write(img, "PNG", out);

                frame.dispose();
                if (onDone != null) onDone.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
        t.setRepeats(false);
        t.start();
    }
}
