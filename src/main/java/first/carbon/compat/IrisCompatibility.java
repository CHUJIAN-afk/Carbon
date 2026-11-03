package first.carbon.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

// 光影访问集中于此；不支持的新版管线保留其原生提交。
public final class IrisCompatibility {
    private boolean checked, active;
    private MethodHandle enabled;
    public boolean canInstance() {
        if (!checked) {
            checked = true;
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                Object instance = api.getMethod("getInstance").invoke(null);
                enabled = MethodHandles.publicLookup().unreflect(api.getMethod("isShaderPackInUse")).bindTo(instance);
            } catch (ReflectiveOperationException ignored) { }
        }
        if (enabled != null) {
            try { active = (boolean) enabled.invokeExact(); }
            catch (Throwable ignored) { active = true; }
        }
        return !active;
    }
}
