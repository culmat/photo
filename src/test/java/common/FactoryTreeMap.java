package common;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * How many times have you written code like this
 * <blockquote><pre>
 *  List&lt;Integer&gt; value = map.get(key);
 *  if(value == null) {
 *    value = new ArrayList&lt;&gt;();
 *    // do something to value ...
 *    map.put(key, value);
 *  }
 * </pre></blockquote>
 * 
 * I always felt there should be a map in the JDK that knew how to create an initialize non existing values.
 * <br/>Here it is:
 * <blockquote><pre>
 *  FactoryTreeMap&lt;String, List&lt;Integer&gt;&gt; map = FactoryTreeMap.create(ArrayList.class);
 * </pre></blockquote>
 * which is equivalent to:
 * <blockquote><pre>
 *  FactoryTreeMap&lt;String, List&lt;Integer&gt;&gt; map = FactoryTreeMap.create(k -&gt; new ArrayList&lt;&gt;());
 * </pre></blockquote>
 * An example using the key to initialize the value:
 * <blockquote><pre>
 *  Map<String, JButton> buttons = FactoryTreeMap.create(text -> new JButton(text));
 * </pre></blockquote>
 * 
 * Of course you can use anonymous classes instead of the create() methods if you prefer. I don't. 
 */
@SuppressWarnings("serial")
public abstract class FactoryTreeMap<K, V> extends TreeMap<K, V> implements Function<K, V> {

	private FactoryTreeMap(Comparator<K> comparator) {
		super(comparator);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TreeMap#get(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		V ret = super.get(key);
		if (ret == null) {
			ret = apply((K) key);
			put((K) key, ret);
		}
		return ret;
	}

	
	public static <K, V> FactoryTreeMap<K, V> create(Function<K, V> function) {
		return create(null, function);
	}
	
	public static <K, V> FactoryTreeMap<K, V> create(Comparator<K> comparator, Function<K, V> function) {
		return new FactoryTreeMap<K, V>(comparator) {
			@Override
			public V apply(K t) {
				return function.apply(t);
			}
		};
	}
	
	public static <K, V> FactoryTreeMap<K, V> create(V defaultValue) {
		return create(null, defaultValue);
	}
	
	public static <K, V> FactoryTreeMap<K, V> create(Comparator<K> comparator, V defaultValue) {
		return create(comparator, ignore -> defaultValue);
	}

	public static <K, V> FactoryTreeMap<K, V> create(Class<? extends V> clazz) {
		return create(null, clazz);
	}
	
	public static <K, V> FactoryTreeMap<K, V> create(Comparator<K> comparator, Class<? extends V> clazz) {
		return create(comparator, key -> instanciate(clazz));
	}

	private static <V> V instanciate(Class<? extends V> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}