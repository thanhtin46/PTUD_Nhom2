package tests;

import ConnectDB.ConnectDB;
import Entity.NhanVien;
import UI.Main_UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class TestMainUiRoleHomepages {

    public static void main(String[] args) {
        System.out.println("========== BẮT ĐẦU KIỂM THỬ TÍCH HỢP TRANG CHỦ THEO ROLE TRONG MAIN_UI ==========");

        ConnectDB.getInstance().connect();

        NhanVien suaChua = new NhanVien("NV02", "Trần Văn Sửa Chữa", "suaChua", "123", "Nhân viên sửa chữa");
        NhanVien kho = new NhanVien("NV03", "Nguyễn Văn Kho", "kho", "123", "Nhân viên kho");
        NhanVien thuNgan = new NhanVien("NV05", "Trần Thị Thu Ngân", "thuNgan", "123", "Nhân viên thu ngân");
        NhanVien tiepTan = new NhanVien("NV01", "Nguyễn Văn Tiếp Tân", "tiepTan", "123", "Nhân viên tiếp tân");

        SwingUtilities.invokeLater(() -> {
            try {
                String artifactDir = new File("output").getAbsolutePath();
                        new File(artifactDir).mkdirs();

                chupMainUi(suaChua, "test_mainui_home_suachua.png", artifactDir, () -> {
                    System.out.println("✔ [1/4] Đã chụp Main_UI cho KTV Sửa chữa");

                    chupMainUi(kho, "test_mainui_home_kho.png", artifactDir, () -> {
                        System.out.println("✔ [2/4] Đã chụp Main_UI cho Thủ kho");

                        chupMainUi(thuNgan, "test_mainui_home_thungan.png", artifactDir, () -> {
                            System.out.println("✔ [3/4] Đã chụp Main_UI cho Thu ngân");

                            chupMainUi(tiepTan, "test_mainui_home_tieptan.png", artifactDir, () -> {
                                System.out.println("✔ [4/4] Đã chụp Main_UI cho Tiếp tân");

                                System.out.println("========== HOÀN THÀNH TOÀN BỘ KIỂM THỬ MAIN_UI THÀNH CÔNG ==========");
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

    private static void chupMainUi(NhanVien nv, String fileName, String dir, Runnable onDone) {
        Main_UI ui = new Main_UI(nv, new ArrayList<>());
        ui.setSize(1400, 850);
        ui.setVisible(true);

        Timer t = new Timer(1800, e -> {
            try {
                BufferedImage img = new BufferedImage(1400, 850, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                ui.paint(g);
                g.dispose();

                File out = new File(dir, fileName);
                ImageIO.write(img, "PNG", out);

                ui.dispose();
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
