package datadog.trace.instrumentation.jdbc;

import static datadog.trace.agent.tooling.ByteBuddyElementMatchers.safeHasInterface;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activateSpan;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.startSpan;
import static datadog.trace.instrumentation.jdbc.JDBCDecorator.DECORATE;
import static datadog.trace.instrumentation.jdbc.JDBCUtils.connectionFromStatement;
import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import com.google.auto.service.AutoService;
import datadog.trace.agent.tooling.Instrumenter;
import datadog.trace.bootstrap.CallDepthThreadLocalMap;
import datadog.trace.bootstrap.instrumentation.api.AgentScope;
import datadog.trace.bootstrap.instrumentation.api.AgentSpan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@AutoService(Instrumenter.class)
public final class PreparedStatementInstrumentation extends Instrumenter.Default {

  public PreparedStatementInstrumentation() {
    super("jdbc");
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return not(isInterface()).and(safeHasInterface(named("java.sql.PreparedStatement")));
  }

  @Override
  public String[] helperClassNames() {
    return new String[] {
      packageName + ".JDBCMaps",
      packageName + ".JDBCUtils",
      "datadog.trace.agent.decorator.BaseDecorator",
      "datadog.trace.agent.decorator.ClientDecorator",
      "datadog.trace.agent.decorator.DatabaseClientDecorator",
      packageName + ".JDBCDecorator",
    };
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    return singletonMap(
        nameStartsWith("execute").and(takesArguments(0)).and(isPublic()),
        PreparedStatementInstrumentation.class.getName() + "$PreparedStatementAdvice");
  }

  public static class PreparedStatementAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static AgentScope onEnter(@Advice.This final PreparedStatement statement) {
      final Connection connection = connectionFromStatement(statement);
      if (connection == null) {
        return null;
      }

      final int callDepth = CallDepthThreadLocalMap.incrementCallDepth(PreparedStatement.class);
      if (callDepth > 0) {
        return null;
      }

      final AgentSpan span = startSpan("database.query");
      DECORATE.afterStart(span);
      DECORATE.onConnection(span, connection);
      DECORATE.onPreparedStatement(span, statement);
      span.setTag("span.origin.type", statement.getClass().getName());
      return activateSpan(span, true);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Enter final AgentScope scope, @Advice.Thrown final Throwable throwable) {
      if (scope == null) {
        return;
      }
      DECORATE.onError(scope.span(), throwable);
      DECORATE.beforeFinish(scope.span());
      scope.close();
      CallDepthThreadLocalMap.reset(PreparedStatement.class);
    }
  }
}
