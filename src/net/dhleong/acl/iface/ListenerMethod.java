package net.dhleong.acl.iface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.dhleong.acl.protocol.ArtemisPacket;
import net.dhleong.acl.world.ArtemisObject;

/**
 * Contains all the information needed to invoke a listener Method (annotated
 * with {@link Listener}).
 * @author rjwut
 */
public class ListenerMethod {
	private Object object;
	private Method method;
	private Class<?> paramType;

	/**
	 * @param object The listener object
	 * @param method The annotated method
	 */
	ListenerMethod (Object object, Method method) {
		validate(method);
		this.object = object;
		this.method = method;
		paramType = method.getParameterTypes()[0];
	}

	/**
	 * Throws an IllegalArgumentException if the given method is not a valid
	 * listener method.
	 */
	private static void validate(Method method) {
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new IllegalArgumentException(
					"Method " + method.getName() +
					" must be public to be a listener"
			);
		}

		if (!Void.TYPE.equals(method.getReturnType())) {
			throw new IllegalArgumentException(
					"Method " + method.getName() +
					" must return void to be a listener"
			);
		}

		Class<?>[] paramTypes = method.getParameterTypes();

		if (paramTypes.length != 1) {
			throw new IllegalArgumentException(
					"Method " + method.getName() +
					" must have exactly one argument to be a listener"
			);
		}

		Class<?> paramType = paramTypes[0];

		if (
				ArtemisPacket.class.isAssignableFrom(paramType) ||
				ArtemisObject.class.isAssignableFrom(paramType) ||
				ConnectionEvent.class.isAssignableFrom(paramType)
		) {
			return;
		}

		throw new IllegalArgumentException(
				"Method " + method.getName() +
				" argument must be assignable to ArtemisPacket," +
				" or ConnectionEvent"
		);
	}

	/**
	 * Returns true if this ListenerMethod accepts events or packets of the
	 * given class; false otherwise.
	 */
	boolean accepts(Class<?> clazz) {
		return paramType.isAssignableFrom(clazz);
	}

	/**
	 * Invokes the wrapped listener Method, passing in the indicated argument,
	 * if it is type-compatible with the Method's argument; otherwise, nothing
	 * happens. Since the listeners have been pre-validated, no exception should
	 * occur, so we wrap the ones thrown by Method.invoke() in a
	 * RuntimeException.
	 */
	void offer(Object arg) {
		Class<?> clazz = arg.getClass();

		if (paramType.isAssignableFrom(clazz)) {
    		try {
				method.invoke(object, arg);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			} catch (IllegalArgumentException ex) {
				throw new RuntimeException(ex);
			} catch (InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}