package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;

/**
 * A bag whose structure and contents have been validated. Once a
 * <code>Bag</code> is validated, it can't be changed. If you want to make
 * changes to it, you must clone it and make changes there. Otherwise, the bag
 * is ready for packaging and transmission.
 * 
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
public class ValidatedBag {

	private Bag myBag;

	ValidatedBag(Bag aBag) {
		aBag.validate();
		myBag = aBag;
	}

	public File toTarBZip2() throws IOException {
		return BagPackager.toTarBZip2(myBag);
	}

	public File toDir() throws IOException {
		return myBag.toDir();
	}

	public File toTar() throws IOException {
		return BagPackager.toTar(myBag);
	}

	public File toTarGz() throws IOException {
		return BagPackager.toTarGz(myBag);
	}

	public File toZip() throws IOException {
		return BagPackager.toZip(myBag);
	}
}
