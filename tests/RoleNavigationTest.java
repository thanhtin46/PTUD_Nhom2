package tests;
import UI.*;
import Entity.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.File;
public class RoleNavigationTest {
    static int checks;
    static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    static Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    public static void main(String[] args)throws Exception {
        try {
            String[] roles={"Quản lý","Nhân viên tiếp tân","Nhân viên sửa chữa","Nhân viên kho","Nhân viên thu ngân","Unknown"};
            for(int i=0;i<roles.length;i++) {
                final int index=i;
                SwingUtilities.invokeAndWait(()->{try {
                    NhanVien n=new NhanVien("NV04","Kiểm thử","quanLy","",roles[index]);
                    Main_UI main=new Main_UI(n,new ArrayList<>());main.setExtendedState(JFrame.NORMAL);main.setSize(1200,780);main.setVisible(true);main.validate();
                    JPanel content=(JPanel)field(main,"contentPanel");Component original=content.getComponent(0);
                    check(index==0?original instanceof TrangChu_UI:original instanceof TrangChuBoPhan_UI,"home "+roles[index]);
                    for(Object row:(ArrayList<?>)field(main,"tatCaNutMenu"))check(QuyenTruyCap.moTrang(n,(String)field(row,"text")),"menu permission");
                    Timer timer=new Timer(40,e->{for(Window w:Window.getWindows())if(w instanceof JDialog && w.isShowing())w.dispose();});timer.start();
                    try {
                        main.chuyenTrang("unknown route");check(content.getComponent(0)==original,"unknown keeps current");
                        if(index>0){main.chuyenTrang("Admin Hub");check(content.getComponent(0)==original,"admin denied");}
                        main.chuyenTrang("Trang chủ",new JPanel());check(content.getComponent(0)==original,"component injection denied");
                    }finally{timer.stop();}
                    BufferedImage image=new BufferedImage(1200,780,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();main.printAll(g);g.dispose();ImageIO.write(image,"png",new File("build/permissions/home-"+index+".png"));
                    main.dispose();
                }catch(Exception ex){throw new RuntimeException(ex);}});
            }
            System.out.println("PASS: "+checks+" navigation/menu checks across 6 roles");System.exit(0);
        }catch(Throwable ex){ex.printStackTrace();System.exit(1);}
    }
}
