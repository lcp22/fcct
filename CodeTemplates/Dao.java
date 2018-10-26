package cn.lcp.fcct.dao;

import cn.lcp.fcct.po.${poEntity};
import org.apache.ibatis.annotations.*;
import cn.lcp.fcct.service.Impl.${poEntity}ServiceImpl;
import java.util.List;

/**
 * @description: 模板生成
 * @author: 模板生成
 * @create: ${creatTime}
 **/
public interface ${poEntity}Dao {
${insertEntity}

${updateEntity}

${selectEntity}

${getEntity}

${deleteEntity}
}
