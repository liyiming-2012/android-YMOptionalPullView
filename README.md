# android-YMOptionalPullView Android下拉刷新库

特点：
1. 仅需1个类文件(YMOptionalPullView.java)即可
2. 接入代码最少、逻辑最简单、兼容性最好、自定义最容易、扩展性最强...


支持说明：
1. 支持上拉、下拉、上下拉
2. 支持自定义样式，通过YMOptionalPullView.TransformViewBuilder接口轻松实现, setTransformViewBuilder()设置
3. 支持RecyclerView、ListView、GridView、WebView、ScrollViw
4. 支持不需要滚动的View, 如LinearLayout、RelativeLayout、FrameLayout、TextView、ImageView...等等统统支持
5. 其他复杂的View可以通过自定义YMOptionalPullView.ContentViewLocator类 setContentViewLocator()方法实现

注意：
因为支持了RecyclerView,所以需要在build.gradle文件的dependencies中引入：
compile 'com.android.support:recyclerview-v7:22.2.1'