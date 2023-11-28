package top.zenyoung.data.mybatis.injector;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

/**
 * 根据ID加载数据(包括被逻辑删除的)
 *
 * @author young
 */
public class SelectPhysicalByIdMethod extends AbstractMethod {

    public SelectPhysicalByIdMethod(){
        super("selectPhysicalById");
    }

    @Override
    public MappedStatement injectMappedStatement(final Class<?> mapperClass, final Class<?> modelClass, final TableInfo tableInfo) {
        //Sql模板
        final String sql = SqlMethod.SELECT_BY_ID.getSql();
        //查询字段
        final String selCols = sqlSelectColumns(tableInfo, false);
        //
        final SqlSource sqlSource = new RawSqlSource(configuration,
                String.format(sql, selCols, tableInfo.getTableName(), tableInfo.getKeyColumn(), tableInfo.getKeyProperty(), ""),
                Object.class);
        return this.addSelectMappedStatementForTable(mapperClass, sqlSource, tableInfo);
    }
}
