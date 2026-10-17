package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * UiIcon — Hệ thống Vector Icon thuần Java2D sắc nét, hiện đại:
 * - 100% vẽ vector bằng Graphics2D, không phụ thuộc font hệ thống, KHÔNG BAO GIỜ bị lỗi ô vuông (tofu ▯)
 * - Tự động đồng bộ màu với foreground của Component (hoặc màu chỉ định)
 * - Khử răng cưa tối đa (Anti-aliasing + Pure Stroke Control)
 */
public class UiIcon implements Icon {
    public static final String PLUS = "plus";
    public static final String TRASH = "trash";
    public static final String CLOCK = "clock";
    public static final String IMPORT = "import";
    public static final String EXPORT = "export";
    public static final String PRINT = "print";
    public static final String CHECK = "check";
    public static final String EDIT = "edit";
    public static final String REFRESH = "refresh";
    public static final String SEARCH = "search";
    public static final String LOCK = "lock";
    public static final String LOGOUT = "logout";
    public static final String CAR = "car";
    public static final String USER = "user";
    public static final String USERS = "users";
    public static final String WRENCH = "wrench";
    public static final String SHIELD = "shield";
    public static final String BOX = "box";
    public static final String BILL = "bill";
    public static final String CLOSE = "close";
    public static final String SETTINGS = "settings";
    public static final String HOME = "home";
    public static final String CHART = "chart";
    public static final String FILE = "file";

    private final String type;
    private final int size;
    private final Color color;

    public UiIcon(String type) {
        this(type, 16, null);
    }

    public UiIcon(String type, int size) {
        this(type, size, null);
    }

    public UiIcon(String type, int size, Color color) {
        this.type = type != null ? type.toLowerCase() : PLUS;
        this.size = Math.max(8, size);
        this.color = color;
    }

    public static UiIcon of(String type) {
        return new UiIcon(type, 16, null);
    }

    public static UiIcon of(String type, int size) {
        return new UiIcon(type, size, null);
    }

    public static UiIcon of(String type, int size, Color color) {
        return new UiIcon(type, size, color);
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Color paintColor = this.color != null ? this.color : (c != null ? c.getForeground() : Color.WHITE);
        g2.setColor(paintColor);

        float s = size / 16.0f;
        g2.translate(x, y);

        switch (type) {
            case PLUS -> {
                g2.setStroke(new BasicStroke(2.2f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Float(8.0f * s, 3.2f * s, 8.0f * s, 12.8f * s));
                g2.draw(new Line2D.Float(3.2f * s, 8.0f * s, 12.8f * s, 8.0f * s));
            }
            case TRASH -> {
                g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Nắp thùng
                g2.draw(new Line2D.Float(3.0f * s, 4.5f * s, 13.0f * s, 4.5f * s));
                g2.draw(new Line2D.Float(6.0f * s, 2.5f * s, 10.0f * s, 2.5f * s));
                // Thân thùng
                Path2D.Float p = new Path2D.Float();
                p.moveTo(4.5f * s, 4.5f * s);
                p.lineTo(5.5f * s, 13.5f * s);
                p.lineTo(10.5f * s, 13.5f * s);
                p.lineTo(11.5f * s, 4.5f * s);
                g2.draw(p);
                // Các vạch sọc giữa
                g2.draw(new Line2D.Float(7.0f * s, 7.0f * s, 7.0f * s, 11.5f * s));
                g2.draw(new Line2D.Float(9.0f * s, 7.0f * s, 9.0f * s, 11.5f * s));
            }
            case CLOCK -> {
                g2.setStroke(new BasicStroke(1.6f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Ellipse2D.Float(2.0f * s, 2.0f * s, 12.0f * s, 12.0f * s));
                g2.draw(new Line2D.Float(8.0f * s, 8.0f * s, 8.0f * s, 4.5f * s));
                g2.draw(new Line2D.Float(8.0f * s, 8.0f * s, 11.2f * s, 8.0f * s));
            }
            case IMPORT -> {
                g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Mũi tên chỉ xuống
                g2.draw(new Line2D.Float(8.0f * s, 2.5f * s, 8.0f * s, 10.5f * s));
                g2.draw(new Line2D.Float(5.2f * s, 7.8f * s, 8.0f * s, 10.5f * s));
                g2.draw(new Line2D.Float(10.8f * s, 7.8f * s, 8.0f * s, 10.5f * s));
                // Khay hứng
                Path2D.Float khay = new Path2D.Float();
                khay.moveTo(3.0f * s, 9.5f * s);
                khay.lineTo(3.0f * s, 13.5f * s);
                khay.lineTo(13.0f * s, 13.5f * s);
                khay.lineTo(13.0f * s, 9.5f * s);
                g2.draw(khay);
            }
            case EXPORT -> {
                g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Mũi tên chỉ lên
                g2.draw(new Line2D.Float(8.0f * s, 10.5f * s, 8.0f * s, 2.5f * s));
                g2.draw(new Line2D.Float(5.2f * s, 5.2f * s, 8.0f * s, 2.5f * s));
                g2.draw(new Line2D.Float(10.8f * s, 5.2f * s, 8.0f * s, 2.5f * s));
                // Khay dưới
                Path2D.Float khay = new Path2D.Float();
                khay.moveTo(3.0f * s, 9.5f * s);
                khay.lineTo(3.0f * s, 13.5f * s);
                khay.lineTo(13.0f * s, 13.5f * s);
                khay.lineTo(13.0f * s, 9.5f * s);
                g2.draw(khay);
            }
            case PRINT -> {
                g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Thân máy in
                g2.draw(new RoundRectangle2D.Float(2.5f * s, 5.0f * s, 11.0f * s, 7.0f * s, 2.0f * s, 2.0f * s));
                // Tờ giấy trên
                Path2D.Float tren = new Path2D.Float();
                tren.moveTo(4.5f * s, 5.0f * s);
                tren.lineTo(4.5f * s, 2.0f * s);
                tren.lineTo(11.5f * s, 2.0f * s);
                tren.lineTo(11.5f * s, 5.0f * s);
                g2.draw(tren);
                // Tờ giấy dưới
                Path2D.Float duoi = new Path2D.Float();
                duoi.moveTo(4.5f * s, 9.5f * s);
                duoi.lineTo(4.5f * s, 14.0f * s);
                duoi.lineTo(11.5f * s, 14.0f * s);
                duoi.lineTo(11.5f * s, 9.5f * s);
                g2.draw(duoi);
                // Đèn báo
                g2.fill(new Ellipse2D.Float(10.5f * s, 6.8f * s, 1.4f * s, 1.4f * s));
            }
            case CHECK -> {
                g2.setStroke(new BasicStroke(2.2f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Float p = new Path2D.Float();
                p.moveTo(3.2f * s, 8.5f * s);
                p.lineTo(6.8f * s, 12.2f * s);
                p.lineTo(13.0f * s, 4.0f * s);
                g2.draw(p);
            }
            case EDIT -> {
                g2.setStroke(new BasicStroke(1.6f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Float pen = new Path2D.Float();
                pen.moveTo(3.0f * s, 13.0f * s);
                pen.lineTo(6.5f * s, 12.5f * s);
                pen.lineTo(13.0f * s, 4.5f * s);
                pen.lineTo(11.5f * s, 3.0f * s);
                pen.lineTo(4.5f * s, 11.0f * s);
                pen.closePath();
                g2.draw(pen);
                g2.draw(new Line2D.Float(2.0f * s, 14.5f * s, 14.0f * s, 14.5f * s));
            }
            case REFRESH -> {
                g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Arc2D.Float arc = new Arc2D.Float(2.5f * s, 2.5f * s, 11.0f * s, 11.0f * s, 40, 275, Arc2D.OPEN);
                g2.draw(arc);
                // Mũi tên
                Path2D.Float head = new Path2D.Float();
                head.moveTo(10.0f * s, 3.0f * s);
                head.lineTo(13.5f * s, 3.0f * s);
                head.lineTo(13.5f * s, 6.5f * s);
                g2.draw(head);
            }
            case SEARCH -> {
                g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Ellipse2D.Float(2.8f * s, 2.8f * s, 7.8f * s, 7.8f * s));
                g2.draw(new Line2D.Float(8.8f * s, 8.8f * s, 13.5f * s, 13.5f * s));
            }
            case LOCK -> {
                g2.setStroke(new BasicStroke(1.6f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Quai khóa
                Path2D.Float quai = new Path2D.Float();
                quai.moveTo(5.0f * s, 7.0f * s);
                quai.lineTo(5.0f * s, 4.5f * s);
                quai.curveTo(5.0f * s, 2.2f * s, 11.0f * s, 2.2f * s, 11.0f * s, 4.5f * s);
                quai.lineTo(11.0f * s, 7.0f * s);
                g2.draw(quai);
                // Thân khóa
                g2.draw(new RoundRectangle2D.Float(3.5f * s, 7.0f * s, 9.0f * s, 7.0f * s, 2.0f * s, 2.0f * s));
                // Ổ khóa
                g2.fill(new Ellipse2D.Float(7.2f * s, 9.2f * s, 1.6f * s, 1.6f * s));
                g2.draw(new Line2D.Float(8.0f * s, 10.5f * s, 8.0f * s, 12.2f * s));
            }
            case LOGOUT -> {
                g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Khung cửa
                Path2D.Float cua = new Path2D.Float();
                cua.moveTo(7.5f * s, 2.5f * s);
                cua.lineTo(3.0f * s, 2.5f * s);
                cua.lineTo(3.0f * s, 13.5f * s);
                cua.lineTo(7.5f * s, 13.5f * s);
                g2.draw(cua);
                // Mũi tên thoát ra
                g2.draw(new Line2D.Float(6.0f * s, 8.0f * s, 13.5f * s, 8.0f * s));
                g2.draw(new Line2D.Float(10.5f * s, 5.0f * s, 13.5f * s, 8.0f * s));
                g2.draw(new Line2D.Float(10.5f * s, 11.0f * s, 13.5f * s, 8.0f * s));
            }
            case CAR -> {
                g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Float car = new Path2D.Float();
                car.moveTo(1.5f * s, 10.0f * s);
                car.lineTo(3.0f * s, 7.0f * s);
                car.lineTo(5.5f * s, 4.0f * s);
                car.lineTo(10.5f * s, 4.0f * s);
                car.lineTo(13.0f * s, 7.0f * s);
                car.lineTo(14.5f * s, 10.0f * s);
                car.lineTo(14.5f * s, 12.0f * s);
                car.lineTo(1.5f * s, 12.0f * s);
                car.closePath();
                g2.draw(car);
                // Bánh xe
                g2.fill(new Ellipse2D.Float(3.5f * s, 10.5f * s, 2.5f * s, 2.5f * s));
                g2.fill(new Ellipse2D.Float(10.0f * s, 10.5f * s, 2.5f * s, 2.5f * s));
            }
            case USER -> {
                g2.setStroke(new BasicStroke(1.6f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Ellipse2D.Float(5.5f * s, 2.2f * s, 5.0f * s, 5.0f * s));
                Path2D.Float vai = new Path2D.Float();
                vai.moveTo(2.5f * s, 13.8f * s);
                vai.curveTo(3.5f * s, 9.5f * s, 12.5f * s, 9.5f * s, 13.5f * s, 13.8f * s);
                g2.draw(vai);
            }
            case USERS -> {
                g2.setStroke(new BasicStroke(1.4f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Người chính
                g2.draw(new Ellipse2D.Float(4.5f * s, 2.5f * s, 4.5f * s, 4.5f * s));
                Path2D.Float vai1 = new Path2D.Float();
                vai1.moveTo(1.8f * s, 13.5f * s);
                vai1.curveTo(2.5f * s, 9.5f * s, 10.5f * s, 9.5f * s, 11.2f * s, 13.5f * s);
                g2.draw(vai1);
                // Người phụ phía sau
                g2.draw(new Ellipse2D.Float(9.5f * s, 3.5f * s, 3.5f * s, 3.5f * s));
                Path2D.Float vai2 = new Path2D.Float();
                vai2.moveTo(11.5f * s, 9.5f * s);
                vai2.curveTo(13.0f * s, 9.5f * s, 14.5f * s, 11.0f * s, 14.5f * s, 13.5f * s);
                g2.draw(vai2);
            }
            case WRENCH -> {
                g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Float(3.0f * s, 13.0f * s, 10.0f * s, 6.0f * s));
                Path2D.Float head = new Path2D.Float();
                head.moveTo(8.5f * s, 7.5f * s);
                head.lineTo(10.5f * s, 3.5f * s);
                head.lineTo(13.0f * s, 4.0f * s);
                head.lineTo(12.0f * s, 7.0f * s);
                head.lineTo(13.5f * s, 9.0f * s);
                head.lineTo(9.5f * s, 10.5f * s);
                g2.draw(head);
            }
            case SHIELD -> {
                g2.setStroke(new BasicStroke(1.6f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Float sh = new Path2D.Float();
                sh.moveTo(2.5f * s, 4.0f * s);
                sh.lineTo(8.0f * s, 2.2f * s);
                sh.lineTo(13.5f * s, 4.0f * s);
                sh.lineTo(13.5f * s, 8.5f * s);
                sh.curveTo(13.5f * s, 12.0f * s, 8.0f * s, 14.2f * s, 8.0f * s, 14.2f * s);
                sh.curveTo(8.0f * s, 14.2f * s, 2.5f * s, 12.0f * s, 2.5f * s, 8.5f * s);
                sh.closePath();
                g2.draw(sh);
            }
            case BOX -> {
                g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Hình hộp 3D
                Path2D.Float box = new Path2D.Float();
                box.moveTo(8.0f * s, 2.0f * s);
                box.lineTo(13.5f * s, 5.0f * s);
                box.lineTo(13.5f * s, 11.5f * s);
                box.lineTo(8.0f * s, 14.5f * s);
                box.lineTo(2.5f * s, 11.5f * s);
                box.lineTo(2.5f * s, 5.0f * s);
                box.closePath();
                g2.draw(box);
                g2.draw(new Line2D.Float(8.0f * s, 2.0f * s, 8.0f * s, 8.0f * s));
                g2.draw(new Line2D.Float(8.0f * s, 8.0f * s, 13.5f * s, 5.0f * s));
                g2.draw(new Line2D.Float(8.0f * s, 8.0f * s, 2.5f * s, 5.0f * s));
                g2.draw(new Line2D.Float(8.0f * s, 8.0f * s, 8.0f * s, 14.5f * s));
            }
            case BILL -> {
                g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new RoundRectangle2D.Float(2.5f * s, 2.5f * s, 11.0f * s, 11.0f * s, 2.0f * s, 2.0f * s));
                g2.draw(new Line2D.Float(5.0f * s, 5.5f * s, 11.0f * s, 5.5f * s));
                g2.draw(new Line2D.Float(5.0f * s, 8.5f * s, 11.0f * s, 8.5f * s));
                g2.draw(new Line2D.Float(5.0f * s, 11.5f * s, 9.0f * s, 11.5f * s));
            }
            case CLOSE -> {
                g2.setStroke(new BasicStroke(2.0f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Float(4.0f * s, 4.0f * s, 12.0f * s, 12.0f * s));
                g2.draw(new Line2D.Float(12.0f * s, 4.0f * s, 4.0f * s, 12.0f * s));
            }
            case SETTINGS, "cog" -> {
                g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Ellipse2D.Float(5.5f * s, 5.5f * s, 5.0f * s, 5.0f * s));
                for (int i = 0; i < 8; i++) {
                    double rad = Math.toRadians(i * 45);
                    float x1 = 8.0f * s + (float)(Math.cos(rad) * 3.8f * s);
                    float y1 = 8.0f * s + (float)(Math.sin(rad) * 3.8f * s);
                    float x2 = 8.0f * s + (float)(Math.cos(rad) * 6.5f * s);
                    float y2 = 8.0f * s + (float)(Math.sin(rad) * 6.5f * s);
                    g2.draw(new Line2D.Float(x1, y1, x2, y2));
                }
            }
            case HOME -> {
                g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Float mai = new Path2D.Float();
                mai.moveTo(2.0f * s, 7.5f * s);
                mai.lineTo(8.0f * s, 2.5f * s);
                mai.lineTo(14.0f * s, 7.5f * s);
                g2.draw(mai);
                Path2D.Float nha = new Path2D.Float();
                nha.moveTo(3.8f * s, 7.2f * s);
                nha.lineTo(3.8f * s, 13.5f * s);
                nha.lineTo(12.2f * s, 13.5f * s);
                nha.lineTo(12.2f * s, 7.2f * s);
                g2.draw(nha);
                g2.draw(new Line2D.Float(6.5f * s, 13.5f * s, 6.5f * s, 9.5f * s));
                g2.draw(new Line2D.Float(6.5f * s, 9.5f * s, 9.5f * s, 9.5f * s));
                g2.draw(new Line2D.Float(9.5f * s, 9.5f * s, 9.5f * s, 13.5f * s));
            }
            case FILE, "document" -> {
                g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Float to = new Path2D.Float();
                to.moveTo(3.2f * s, 2.2f * s);
                to.lineTo(9.5f * s, 2.2f * s);
                to.lineTo(12.8f * s, 5.5f * s);
                to.lineTo(12.8f * s, 13.8f * s);
                to.lineTo(3.2f * s, 13.8f * s);
                to.closePath();
                g2.draw(to);
                g2.draw(new Line2D.Float(9.5f * s, 2.2f * s, 9.5f * s, 5.5f * s));
                g2.draw(new Line2D.Float(9.5f * s, 5.5f * s, 12.8f * s, 5.5f * s));
                g2.draw(new Line2D.Float(5.5f * s, 8.2f * s, 10.5f * s, 8.2f * s));
                g2.draw(new Line2D.Float(5.5f * s, 10.8f * s, 9.2f * s, 10.8f * s));
            }
            case CHART, "barchart" -> {
                g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Float(2.5f * s, 13.8f * s, 13.5f * s, 13.8f * s));
                g2.draw(new Line2D.Float(2.5f * s, 2.5f * s, 2.5f * s, 13.8f * s));
                g2.fill(new RoundRectangle2D.Float(4.5f * s, 9.0f * s, 2.0f * s, 4.8f * s, 1.0f * s, 1.0f * s));
                g2.fill(new RoundRectangle2D.Float(7.5f * s, 5.5f * s, 2.0f * s, 8.3f * s, 1.0f * s, 1.0f * s));
                g2.fill(new RoundRectangle2D.Float(10.5f * s, 3.2f * s, 2.0f * s, 10.6f * s, 1.0f * s, 1.0f * s));
            }
            default -> {
                g2.setStroke(new BasicStroke(2.0f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Float(8.0f * s, 3.5f * s, 8.0f * s, 12.5f * s));
                g2.draw(new Line2D.Float(3.5f * s, 8.0f * s, 12.5f * s, 8.0f * s));
            }
        }

        g2.dispose();
    }
}
