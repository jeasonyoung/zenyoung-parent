package top.zenyoung.jpa.reactive.querydsl.apt;

import com.google.common.base.CaseFormat;
import com.querydsl.codegen.EntityType;
import com.querydsl.sql.SchemaAndTable;
import com.querydsl.sql.codegen.NamingStrategy;
import com.querydsl.sql.codegen.support.ForeignKeyData;

public class R2dbcNamingStrategy implements NamingStrategy {

    @Override
    public String appendSchema(final String packageName, final String schema) {
        return null;
    }

    @Override
    public String getClassName(final String tableName) {
        return null;
    }

    @Override
    public String getClassName(final SchemaAndTable schemaAndTable) {
        return null;
    }

    @Override
    public String getDefaultAlias(final EntityType entityType) {
        return entityType.getSimpleName();
    }

    @Override
    public String getDefaultVariableName(final EntityType entityType) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, getDefaultAlias(entityType));
    }

    @Override
    public String getForeignKeysClassName() {
        return null;
    }

    @Override
    public String getForeignKeysVariable(final EntityType entityType) {
        return null;
    }

    @Override
    public String getPrimaryKeysClassName() {
        return null;
    }

    @Override
    public String getPrimaryKeysVariable(final EntityType entityType) {
        return null;
    }

    @Override
    public String getPropertyName(final String columnName, final EntityType entityType) {
        return null;
    }

    @Override
    public String getPropertyNameForForeignKey(final String foreignKeyName, final EntityType entityType) {
        return null;
    }

    @Override
    public String getPropertyNameForInverseForeignKey(final String name, final EntityType model) {
        return null;
    }

    @Override
    public String getPropertyNameForPrimaryKey(final String name, final EntityType model) {
        return null;
    }

    @Override
    public String normalizeColumnName(final String columnName) {
        return null;
    }

    @Override
    public String normalizeTableName(final String tableName) {
        return null;
    }

    @Override
    public String normalizeSchemaName(final String schemaName) {
        return null;
    }

    @Override
    public boolean shouldGenerateClass(final SchemaAndTable schemaAndTable) {
        return false;
    }

    @Override
    public boolean shouldGenerateForeignKey(final SchemaAndTable schemaAndTable, final ForeignKeyData foreignKeyData) {
        return false;
    }

    @Override
    public String getPackage(final String basePackage, final SchemaAndTable schemaAndTable) {
        return null;
    }
}