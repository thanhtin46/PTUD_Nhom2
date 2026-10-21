package Entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bản ghi nhật ký kiểm toán hệ thống (Audit Log / Activity Trail).
 * Lưu lại mọi hành động trọng yếu của các tài khoản để Admin giám sát và kiểm tra an ninh.
 */
public class NhatKyHeThong implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String CRITICAL = "CRITICAL";

    private String thoiGian;
    private String nguoiThucHien;
    private String vaiTro;
    private String hanhDong;
    private String mucDo;
    private String chiTiet;

    public NhatKyHeThong(String nguoiThucHien, String vaiTro, String hanhDong, String mucDo, String chiTiet) {
        this.thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        this.nguoiThucHien = nguoiThucHien;
        this.vaiTro = vaiTro;
        this.hanhDong = hanhDong;
        this.mucDo = mucDo;
        this.chiTiet = chiTiet;
    }

    public NhatKyHeThong(String thoiGian, String nguoiThucHien, String vaiTro, String hanhDong, String mucDo, String chiTiet) {
        this.thoiGian = thoiGian;
        this.nguoiThucHien = nguoiThucHien;
        this.vaiTro = vaiTro;
        this.hanhDong = hanhDong;
        this.mucDo = mucDo;
        this.chiTiet = chiTiet;
    }

    public String getThoiGian() { return thoiGian; }
    public String getNguoiThucHien() { return nguoiThucHien; }
    public String getVaiTro() { return vaiTro; }
    public String getHanhDong() { return hanhDong; }
    public String getMucDo() { return mucDo; }
    public String getChiTiet() { return chiTiet; }
}
