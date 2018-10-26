package cn.lcp.fcct;

import cn.lcp.fcct.dao.CodeFactoryDao;
import cn.lcp.fcct.po.InformationSchema;
import cn.lcp.fcct.util.CodeFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("cn.lcp.fcct.dao")
public class FcctApplicationTests {
    @Autowired
    private CodeFactoryDao codeFactoryDao;

    @Test
    public void contextLoads() {
        long t = System.currentTimeMillis();
        System.out.println("-------------------------开始------------------------------");
        CodeFactory codeFactory = CodeFactory.getInstance();
        //获取所有表名
        List<String> tableNames = codeFactoryDao.getTableName();
        for (String tableName:tableNames) {
            //获取表相关属性
            List<InformationSchema> informationSchemas = codeFactoryDao.getColumnName(tableName);
            //创建文件
            codeFactory.CreateCode(tableName,informationSchemas);
            break;
        }
        System.out.println("-------------------------结束------------------------------");
        System.out.println("----------------耗时:"+(System.currentTimeMillis()-t)+"毫秒------------------------------");
    }

}
