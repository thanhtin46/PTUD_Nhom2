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

public class TestDieuHanhTuongTac {

    public static void main(String[] args) {
        System.out.println("========== KIỂM THỬ TƯƠNG TÁC & PHÂN QUYỀN ==========");

        ConnectDB.getInstance().connect();

        // 1. Kiểm tra phân quyền menu Main_UI
        NhanVien quanLy = new NhanVien("NV04", "Quản Lý Tổng", "quanLy", "123", "Quản lý");
        NhanVien tiepTan = new NhanVien("NV01", "Nguyễn Văn Tiếp Tân", "tiepTan", "123", "Nhân viên tiếp tân");
        NhanVien ktv = new NhanVien("NV02", "Trần Văn Sửa Chữa", "suaChua", "123", "Nhân viên sửa chữa");

        SwingUtilities.invokeLater(() -> {
            try {
                // Kiểm tra Main_UI cho Quản lý
                Main_UI uiQL = new Main_UI(quanLy, new ArrayList<>());
                boolean coMenuQL = coChucNangDieuHanh(uiQL);
                System.out.println("✔ [TEST ROLE 1] Tài khoản Quản lý (NV04) thấy menu 'Điều hành sửa chữa': " + coMenuQL);
                if (!coMenuQL) {
                    System.err.println("❌ LỖI: Quản lý không thấy menu Điều hành sửa chữa!");
                }
                uiQL.dispose();

                // Kiểm tra Main_UI cho Tiếp tân
                Main_UI uiTT = new Main_UI(tiepTan, new ArrayList<>());
                boolean coMenuTT = coChucNangDieuHanh(uiTT);
                System.out.println("✔ [TEST ROLE 2] Tài khoản Tiếp tân (NV01) KHÔNG thấy menu 'Điều hành sửa chữa': " + (!coMenuTT));
                if (coMenuTT) {
                    System.err.println("❌ LỖI BẢO MẬT: Tiếp tân thấy menu Điều hành sửa chữa!");
                }
                uiTT.dispose();

                // Kiểm tra Main_UI cho KTV
                Main_UI uiKTV = new Main_UI(ktv, new ArrayList<>());
                boolean coMenuKTV = coChucNangDieuHanh(uiKTV);
                System.out.println("✔ [TEST ROLE 3] Tài khoản KTV (NV02) KHÔNG thấy menu 'Điều hành sửa chữa': " + (!coMenuKTV));
                if (coMenuKTV) {
                    System.err.println("❌ LỖI BẢO MẬT: KTV thấy menu Điều hành sửa chữa!");
                }
                uiKTV.dispose();

                // 2. Mở Main_UI của Quản lý và chuyển tới Điều hành sửa chữa, chụp ảnh toàn màn hình
                Main_UI uiMain = new Main_UI(quanLy, new ArrayList<>());
                uiMain.setSize(1400, 850);
                uiMain.setVisible(true);
                uiMain.chuyenTrang("Điều hành sửa chữa");

                Timer t = new Timer(2000, e -> {
                    try {
                        BufferedImage img = new BufferedImage(1400, 850, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = img.createGraphics();
                        uiMain.paint(g);
                        g.dispose();

                        String artifactDir = new File("output").getAbsolutePath();
                        new File(artifactDir).mkdirs();
                        File out = new File(artifactDir, "test_dieu_hanh_sua_chua_full_main.png");
                        ImageIO.write(img, "PNG", out);
                        System.out.println("✔ Đã lưu ảnh chụp toàn màn hình Main_UI: " + out.getAbsolutePath());

                        uiMain.dispose();
                        System.out.println("========== TOÀN BỘ KIỂM THỬ PHÂN QUYỀN & TƯƠNG TÁC THÀNH CÔNG ==========");
                        System.exit(0);
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

    private static boolean coChucNangDieuHanh(Container container) {
        for (Component c : container.getComponents()) {
            if (c.getClass().getSimpleName().equals("MenuRow")) {
                try {
                    java.lang.reflect.Field f = c.getClass().getDeclaredField("text");
                    f.setAccessible(true);
                    String t = (String) f.get(c);
                    if (t != null && t.contains("Điều hành sửa chữa")) {
                        return true;
                    }
                } catch (Exception ignored) {}
            }
            if (c instanceof Container) {
                if (coChucNangDieuHanh((Container) c)) {
                    return true;
                }
            }
        }
        return false;
    }
}
