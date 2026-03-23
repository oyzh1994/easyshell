package cn.oyzh.easyshell.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PackageSpecificClassLoader extends ClassLoader {

    private final Map<String, Class<?>> loadedClasses = new HashMap<>();  // 记录已加载的类，避免重复

    public PackageSpecificClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
//        clazz = getParent().loadClass(name);
//        if (clazz != null) {
//            return clazz;
//        }

//        if (!name.startsWith("java.") && !name.startsWith("javax.") &&
//                !name.startsWith("sun.")) {
        if (name.startsWith("cn.oyzh.")) {
            // 2. 如果是目标包下的类，尝试自己加载
            try {
                clazz = loadedClasses.get(name);
                if (clazz == null) {
                    clazz = findClass(name);
                    if (clazz != null) {
                        loadedClasses.put(name, clazz);
                        return clazz;
                    }
                }
            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
                // 自己加载失败，交给父类加载器（一般不会发生，但如果类不存在，也要允许父类尝试）
            }
        }


        // 3. 其他情况委托给父类加载器
        return super.loadClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 将类名转换为文件路径
        String fileName = name.replace('.', '/').concat(".class");
        InputStream is = getResourceAsStream(fileName);
        if (is != null) {
            try {
                byte[] bytes = is.readAllBytes();
                return defineClass(name, bytes, 0, bytes.length);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return super.findClass(name);
    }
}