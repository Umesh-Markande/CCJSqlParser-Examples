package com.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.AddAliasesVisitor;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class CCJSqlExample {

	public static void main(String[] args) throws JSQLParserException {
		String varname1 = "SELECT  hallprefs.studentname AS fdsf , "
				+ "                hh.studentid AS dfsd , "
				+ "                hh.studentdesc , Max(gkhk) , Min( jgjg), "
				+ "                hh.hallname, "
				+ "                ( SELECT te FROM ta  WHERE  fsdf = fsdf) , "
				+ "                ffsdf fd , "
				+ "                ferwfsdfs frere "
				+ "FROM            students "
				+ "INNER JOIN      hallprefs "
				+ "ON              ss.studentid = hallprefs.studentid "
				+ "LEFT OUTER JOIN fdsff "
				+ "ON              fsdfsfd = fsff "
				+ "INNER JOIN      halls hh \" + \" "
				+ "ON              hp.hallid = hh.hallid "
				+ "CROSS JOIN ( SELECT tst   FROM   tsdfs) "
			     // + " cross join dfdf where gh=jgjh ";
				+ "WHERE gh=jgjh "
				+ "AND   tsfds = sdfd "
				+ "OR    fsdfsf = fdsff "
				+ "AND   ( fdsfsf = fdsf OR  fdsfsf = fdfsf) "
				+ "AND  lfsdf LIKE '%fdsf%' "
				+ "AND  ( fsdfs BETWEEN 99 AND 778) "
				+ "AND  fsdfs < 877";
				

		Statement st = CCJSqlParserUtil.parse(varname1);
		Select select = (Select) st;

		// set aliase if not provide

		AddAliasesVisitor instance = new AddAliasesVisitor();
		instance.setPrefix("A");
		select.getSelectBody().accept(instance);

		PlainSelect ps = (PlainSelect) select.getSelectBody();

		System.out.println(ps.toString());

		// select column item visitor with sub select

		List<String> selectColumnAliase = new ArrayList<>();

		for (SelectItem item : ((PlainSelect) select.getSelectBody()).getSelectItems()) {
			if (item instanceof SelectExpressionItem) {
				Expression expr = ((SelectExpressionItem) item).getExpression();
				if (item instanceof SelectExpressionItem) {
					Expression subexpr = ((SelectExpressionItem) item).getExpression();
					if (expr instanceof SubSelect) {
						// ((SubSelect)expr).getSelectBody()) // iterat sub select item
						System.out.println("sub select");
					}
				}
				System.out.println(((SelectExpressionItem) item).getAlias().getName());
				if (expr instanceof Column) {
					Column col = (Column) expr;
					// System.out.println(col.getColumnName()); // only column name without alise
					// hh.dffs output dffs
					Table table = col.getTable();
					System.out.println(col.getName(true));
					// System.out.println(table.getFullyQualifiedName());
					selectColumnAliase.add("".equals(table.getFullyQualifiedName()) ? col.getColumnName()
							: table.getFullyQualifiedName());
				}
			}
		}

		// from table visitor

		FromItem fromItem = ps.getFromItem();

		String fromAliase = fromItem.getAlias() != null ? fromItem.getAlias().toString() : "";

		System.out.println(fromAliase);

		// join sub select item visitor

		select.getSelectBody().accept(new SelectVisitorAdapter() {
			@Override
			public void visit(PlainSelect plainSelect) {
				plainSelect.getFromItem().accept(fromJoinSubSelectVisitor);
				if (plainSelect.getJoins() != null) {
					plainSelect.getJoins().forEach(join -> join.getRightItem().accept(fromJoinSubSelectVisitor));
				}
			}
		});

		// join table visitor

		List<Integer> removeJoinIndex = new ArrayList<>();
		int index = 0;
		for (Iterator<Join> joinsIt = ps.getJoins().iterator(); joinsIt.hasNext();) {
			Join join = (Join) joinsIt.next();
			System.out.println(join.getRightItem().getAlias());
			if (join.getRightItem() != null) {
				String aliase = join.getRightItem().getAlias() != null ? join.getRightItem().getAlias().getName()
						: join.getRightItem().toString();
				if (!selectColumnAliase.contains(aliase) && !aliase.toLowerCase().contains("select")) {
					removeJoinIndex.add(index);
				}
			}
			index++;
		}
		for (Integer integer : removeJoinIndex) {
			ps.getJoins().remove(integer.intValue());
		}

		// where condition visitor
		ps.setWhere(new StringValue("{{" + parseWhere(ps.getWhere().toString()) + "}}"));

		String query = ps.toString();
		query = query.replace("'{{", "");
		query = query.replace("}}'", "");
		System.out.println(query);

	}

	private static void processJoinItem(FromItem fromItem) {
		System.out.println("fromItem=" + fromItem);
	}

	private final static FromItemVisitorAdapter fromJoinSubSelectVisitor = new FromItemVisitorAdapter() {
		@Override
		public void visit(SubSelect subSelect) {
			System.out.println("subselect=" + subSelect);
			PlainSelect ps = (PlainSelect) subSelect.getSelectBody();
			/*
			 * String whereCondition = String.valueOf(ps.getWhere()); if(ps.getWhere() !=
			 * null) { // iterate where condition }
			 */

		}

		@Override
		public void visit(Table table) {
			System.out.println("table=" + table);
		}

		public void visit(SubJoin subjoin) {
			System.out.println(subjoin.toString());
		}
	};

	public static String parseWhere(String where) throws JSQLParserException {
		Expression expr = CCJSqlParserUtil.parseCondExpression(where);
		StringBuilder b = new StringBuilder();
		expr.accept(new ExpressionDeParser(null, b) {
			int depth = 0;

			@Override
			public void visit(Parenthesis parenthesis) {
				if (parenthesis.isNot()) {
					getBuffer().append("NOT");
				}
				depth++;
				parenthesis.getExpression().accept(this);
				depth--;
			}

			@Override
			public void visit(OrExpression orExpression) {
				visitBinaryExpr(orExpression, "OR");
			}

			@Override
			public void visit(AndExpression andExpression) {
				visitBinaryExpr(andExpression, "AND");
			}

			private void visitBinaryExpr(BinaryExpression expr, String operator) {
				if (expr.isNot()) {
					getBuffer().append("NOT");
				}
				if (!(expr.getLeftExpression() instanceof OrExpression)
						&& !(expr.getLeftExpression() instanceof AndExpression)
						&& !(expr.getLeftExpression() instanceof Parenthesis)) {
					String string = expr.getLeftExpression().toString();
					System.out.println(string);
				}
				expr.getLeftExpression().accept(this);
				if (!(expr.getRightExpression() instanceof OrExpression)
						&& !(expr.getRightExpression() instanceof AndExpression)
						&& !(expr.getRightExpression() instanceof Parenthesis)) {
					String string = expr.getRightExpression().toString();
					System.out.println(string);
				}
				expr.getRightExpression().accept(this);
			}
		});
		return expr.toString();
	}

	protected static String getEntityAlias(String entityName, PlainSelect query) {
		/*
		 * for (Object o : query.getSelectItems()) { SelectItem st = (SelectItem) o; if
		 * (hasEntityAlias(entityName, st)) { return
		 * join.getRightItem().getAlias().toString(); } }
		 */
		FromItem fromItem = query.getFromItem();
		if (hasEntityAlias(entityName, fromItem)) {
			return fromItem.getAlias().toString();
		}
		if (query.getJoins() != null) {
			for (Object o : query.getJoins()) {
				Join join = (Join) o;
				if (hasEntityAlias(entityName, join.getRightItem())) {
					return join.getRightItem().getAlias().toString();
				}
			}
		}
		return null;
	}

	private static boolean hasEntityAlias(String entityName, FromItem fromItem) {
		return fromItem instanceof net.sf.jsqlparser.schema.Table
				&& ((net.sf.jsqlparser.schema.Table) fromItem).getName().equals(entityName)
				&& !StringUtils.isBlank(fromItem.getAlias().toString());
	}
	/*
	 * private static boolean hasEntityAlias(String entityName, SelectItem
	 * selectitem) { return selectitem instanceof net.sf.jsqlparser.schema.Column &&
	 * ((net.sf.jsqlparser.schema.Column) selectitem).getName().equals(entityName)
	 * && !StringUtils.isBlank(selectitem. .toString()); }
	 */
}
