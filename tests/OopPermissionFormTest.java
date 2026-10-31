package tests;

import Entity.QuyenTruyCap;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.*;

/** Kiểm tra cấu hình sự kiện và quyền mặc định trên form thật; không bấm lưu. */
public class OopPermissionFormTest {
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }
    private static Object field(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }
    private static void checkReset(Container container) {
        for (Component child : container.getComponents()) {
            if (child instanceof JButton b && "Khôi phục theo vai trò".equals(b.getText())) {
                check(b.getActionListeners().length == 1, "Reset fires once");
            }
            if (child instanceof Container c) checkReset(c);
        }
    }
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        String url = System.getProperty("gara.db.url", "");
        if (!url.contains("databaseName=GaraSyncTest_Report_20261028;"))
            throw new IllegalArgumentException("Explicit test database required");
        var connection = ConnectDB.ConnectDB.getInstance().getConnection();
        if (connection == null || !"GaraSyncTest_Report_20261028".equals(connection.getCatalog()))
            throw new IllegalStateException("Test database unavailable");
        SwingUtilities.invokeAndWait(() -> {
            try {
                UI.PhanQuyen_UI form = new UI.PhanQuyen_UI();
                JButton save = (JButton) field(form, "btnLuu");
                check(save.getActionListeners().length == 1, "Save fires once");
                checkReset(form);
                JComboBox<String> roles = (JComboBox<String>) field(form, "cboVaiTro");
                Map<QuyenTruyCap.Quyen, JCheckBox> rights =
                        (Map<QuyenTruyCap.Quyen, JCheckBox>) field(form, "oQuyen");
                for (int i = 0; i < roles.getItemCount(); i++) {
                    roles.setSelectedIndex(i);
                    var defaults = QuyenTruyCap.macDinh((String) roles.getSelectedItem());
                    for (var right : rights.entrySet()) {
                        check(right.getValue().getText().equals(right.getKey().getTenHienThi()), "Label matches enum");
                        if (defaults.contains(right.getKey()))
                            check(right.getValue().isSelected() && !right.getValue().isEnabled(), "Default right is fixed");
                    }
                }
                roles.setSelectedItem("Nhân viên kho");
                JFrame frame = new JFrame("Kiểm tra form phân quyền");
                try {
                    frame.setContentPane(form);
                    frame.setSize(1280, 720);
                    frame.setVisible(true);
                    java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                            frame.getWidth(), frame.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                    var graphics = image.createGraphics();
                    frame.printAll(graphics);
                    graphics.dispose();
                    javax.imageio.ImageIO.write(image, "png", new java.io.File("build/oop/phan-quyen.png"));
                } finally { frame.dispose(); }
            } catch (Exception ex) { throw new RuntimeException(ex); }
        });
        connection.close();
        System.out.println("PASS: " + checks + " Swing permission-form checks; no save action executed");
    }
}
