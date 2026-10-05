package UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/** Tiện ích tải ảnh linh hoạt: thử resources/, sau đó file trong thư mục dự án. */
public final class HinhAnh {

    private HinhAnh() {}

    public static ImageIcon icon(String ten, int rong, int cao) {
        Image img = doc(ten);
        if (img == null) return null;
        return new ImageIcon(img.getScaledInstance(rong, cao, Image.SCALE_SMOOTH));
    }

    public static Image doc(String ten) {
        // 1) thử classpath (khi chạy trong IntelliJ, src là source root nên /<ten> có thể không có; thử 2) trước)
        try (InputStream in = HinhAnh.class.getResourceAsStream("/" + ten)) {
            if (in != null) return ImageIO.read(in);
        } catch (Exception ignored) { }
        // 2) thử thư mục dự án: src/images/<ten> và src/<ten>
        for (String p : new String[]{
                "src/images/" + ten,
                "src/" + ten,
                "images/" + ten,
                ten}) {
            File f = new File(p);
            if (f.exists()) {
                try { return ImageIO.read(f); }
                catch (Exception ignored) { }
            }
        }
        // 3) tạo ảnh nền mặc định nếu không tìm thấy
        return anhGaraMacDinh(900, 600);
    }

    /** Vẽ ảnh gara placeholder (xe + cầu nâng) đẹp mắt bằng Java2D. */
    public static Image anhGaraMacDinh(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // nền
        GradientPaint gp = new GradientPaint(0, 0, new Color(220, 232, 246), 0, h, new Color(184, 210, 240));
        g.setPaint(gp);
        g.fillRect(0, 0, w, h);

        // tường phía sau
        g.setColor(new Color(210, 222, 236));
        g.fillRect(0, 0, w, h / 2);

        // sàn
        g.setColor(new Color(150, 165, 180));
        g.fillRect(0, h * 2 / 3, w, h / 3);
        g.setColor(new Color(120, 135, 150));
        g.drawLine(0, h * 2 / 3, w, h * 2 / 3);

        // cầu nâng (2 cột)
        g.setColor(new Color(30, 55, 100));
        int cotX1 = w / 4, cotX2 = w * 3 / 4, cotY1 = h / 5, cotY2 = h * 2 / 3;
        g.fillRect(cotX1 - 14, cotY1, 28, cotY2 - cotY1);
        g.fillRect(cotX2 - 14, cotY1, 28, cotY2 - cotY1);
        g.setColor(new Color(60, 90, 150));
        g.fillRect(cotX1 - 18, cotY2, 36, 10);
        g.fillRect(cotX2 - 18, cotY2, 36, 10);

        // thanh ngang trên cùng
        g.setColor(new Color(30, 55, 100));
        g.fillRect(cotX1, cotY1, cotX2 - cotX1, 12);

        // thân xe
        int xeX = w / 6, xeY = h / 3, xeW = w * 2 / 3, xeH = h / 3;
        g.setColor(new Color(200, 40, 40));
        g.fillRoundRect(xeX, xeY + 20, xeW, xeH - 30, 20, 20);
        // cabin
        g.setColor(new Color(180, 30, 30));
        g.fillRoundRect(xeX + xeW / 4, xeY - 10, xeW / 2, 60, 20, 20);
        // kính
        g.setColor(new Color(180, 220, 240, 220));
        g.fillRoundRect(xeX + xeW / 4 + 10, xeY, xeW / 2 - 20, 35, 14, 14);

        // bánh xe
        g.setColor(new Color(30, 30, 30));
        g.fillOval(xeX + 30, xeY + xeH - 40, 70, 70);
        g.fillOval(xeX + xeW - 100, xeY + xeH - 40, 70, 70);
        g.setColor(new Color(220, 220, 220));
        g.fillOval(xeX + 45, xeY + xeH - 25, 40, 40);
        g.fillOval(xeX + xeW - 85, xeY + xeH - 25, 40, 40);

        // đèn pha
        g.setColor(new Color(255, 240, 180));
        g.fillOval(xeX + xeW - 25, xeY + 40, 20, 20);

        // dụng cụ treo tường
        g.setColor(new Color(90, 100, 110));
        for (int i = 0; i < 4; i++) {
            g.fillRect(60, 60 + i * 35, 50, 6);
            g.fillOval(120, 56 + i * 35, 14, 14);
        }

        // đồng hồ
        g.setColor(Color.WHITE);
        g.fillOval(w - 130, 50, 60, 60);
        g.setColor(new Color(60, 70, 80));
        g.drawOval(w - 130, 50, 60, 60);
        g.setStroke(new BasicStroke(3));
        g.drawLine(w - 100, 80, w - 100, 60);
        g.drawLine(w - 100, 80, w - 80, 80);

        g.dispose();
        return img;
    }
}
