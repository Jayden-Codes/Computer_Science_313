import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This Serializer Class converts objects to byte arrays and vice versa
 * 
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */
public class Serializer {
	/**
	 * This method returns a byte array.
	 * 
	 * @param obj - the object to be written.
	 * @return - a byte array
	 */
	public static byte[] toBytes(Object obj) {
		try {
			/* Take in object and convert to byte array */
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(b);
			o.writeObject(obj);
			return b.toByteArray();

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * This method returns an Object.
	 * 
	 * @param bytes - a byte array.
	 * @return - a object.
	 */
	public static Object toObject(byte[] bytes) {
		try {
			/* Take in byte array and convert to object */
			ByteArrayInputStream b = new ByteArrayInputStream(bytes);
			ObjectInputStream o = new ObjectInputStream(b);
			return o.readObject();
		} catch (Exception e) {
			return null;
		}
	}

}
