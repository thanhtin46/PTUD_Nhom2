package tests;

import DAO.PhieuBaoHanh_DAO;
import java.lang.reflect.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class PhieuBaoHanhMergeTest {
    static class Db implements InvocationHandler {
        boolean auto = true, committed, rolledBack, failBatch, duplicate, wrongVehicle;
        final Map<Integer,Object> header = new HashMap<>();
        Connection connection() { return proxy(Connection.class, this); }
        public Object invoke(Object p, Method m, Object[] args) throws Throwable {
            return switch(m.getName()) {
                case "isClosed" -> false;
                case "getAutoCommit" -> auto;
                case "setAutoCommit" -> { auto = (boolean) args[0]; yield null; }
                case "commit" -> { committed = true; yield null; }
                case "rollback" -> { rolledBack = true; yield null; }
                case "prepareStatement" -> statement((String) args[0]);
                default -> fallback(m);
            };
        }
        PreparedStatement statement(String sql) {
            return proxy(PreparedStatement.class, (p,m,a) -> {
                if (m.getName().startsWith("set") && sql.startsWith("INSERT INTO dbo.PhieuBaoHanh")) {
                    header.put((Integer)a[0], a[1]);
                }
                return switch(m.getName()) {
                    case "executeQuery" -> {
                        boolean[] next = {true};
                        yield proxy(ResultSet.class, (r,rm,ra) -> switch(rm.getName()) {
                            case "next" -> { boolean value=next[0]; next[0]=false; yield value; }
                            case "getInt" -> duplicate ? 1 : 0;
                            case "getDate" -> java.sql.Date.valueOf(LocalDate.now());
                            default -> fallback(rm);
                        });
                    }
                    case "executeUpdate" -> wrongVehicle ? 0 : 1;
                    case "executeBatch" -> {
                        if(failBatch) throw new SQLException("Injected detail failure");
                        yield new int[]{1,1};
                    }
                    default -> fallback(m);
                };
            });
        }
    }
    static <T> T proxy(Class<T> type, InvocationHandler h) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},h));
    }
    static Object fallback(Method m) {
        if(m.getReturnType()==boolean.class) return false;
        if(m.getReturnType()==int.class) return 0;
        return null;
    }
    static void check(boolean value,String msg) { if(!value) throw new AssertionError(msg); }
    static void save(Db db, boolean badDate) throws SQLException {
        LocalDate start=LocalDate.now();
        List<Object[]> details=new ArrayList<>();
        details.add(new Object[]{"Lọc dầu", 6, java.sql.Date.valueOf(start.plusMonths(6)), "", ""});
        details.add(new Object[]{"Động cơ", 12, java.sql.Date.valueOf(start.plusMonths(badDate ? 11 : 12)), "", ""});
        new PhieuBaoHanh_DAO(db.connection()).themPhieuBaoHanh(
            "PBH00001","HD001","NV001","51A-12345",java.sql.Date.valueOf(start),"Tạm ngưng","Ghi chú",details);
    }
    public static void main(String[] args) throws Exception {
        Db ok=new Db(); save(ok,false);
        check(ok.committed && !ok.rolledBack && ok.auto,"Successful transaction");
        check(java.sql.Date.valueOf(LocalDate.now().plusMonths(12)).equals(ok.header.get(4)),"Header expiry must cover longest item");
        check("TAM_NGUNG".equals(ok.header.get(5)),"Status must retain paused state");
        for(int scenario=0;scenario<4;scenario++) {
            Db db=new Db(); db.failBatch=scenario==0; db.duplicate=scenario==1; db.wrongVehicle=scenario==2;
            try { save(db,scenario==3); throw new AssertionError("Expected failure "+scenario); }
            catch(SQLException expected) { check(db.rolledBack && !db.committed && db.auto,"Rollback/restore "+scenario); }
        }
        System.out.println("PASS: save, max expiry, status, detail rollback, duplicate, wrong vehicle, invalid expiry");
    }
}