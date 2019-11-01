## NeButterKnife 手写ButterKnife框架
### 思路
`ButterKnife.bind(this)`找到`MainActivity$ViewBinder`这个实现了`ViewBinder<T>`接口的类，然后通过反射调用
其接口方法`bind(T t)`.  
`compiler`下的`ButterKnifeProcessor`注解处理器的作用就是来生成`MainActivity$ViewBinder`这个类的：在编译时，
注解处理器找到所有带`@BindView/@Onclick`注解的属性元素集合,放到以完整包名为键的`Map<String, List<ExecutableElement>>` 
Map中，然后通过`JavaFileObject`生成`MainActivity$ViewBinder`类，该类实现了`ViewBinder`接口，在其`bind`方法
中完成了`findViewById(R.id.xx)`和`setOnClickListener`的工作。
