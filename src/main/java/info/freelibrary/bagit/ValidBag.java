package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;

/**
 * Bag whose structure is complete and whose contents have been validated. Once
 * a <code>Bag</code> is validated, it can't be changed. If you want to make
 * changes to it, you must clone it and make changes there. Otherwise, the bag
 * is ready for packaging and transmission.
 * 
 * <div>A <code>ValidBag</code> is constructed using a <code>BagValidator</code>
 * . For instance: <blockquote>
 * <code>Bag bag = new Bag("papas_got_a_brand_new").complete();</code>
 * <code>ValidBag validBag = new BagValidator().validate(bag);</code>
 * </blockquote> A validated bag can then be used to create a bag file that is
 * ready for transmission.</div>
 * 
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
public class ValidBag {

	private Bag myBag;

	ValidBag(Bag aBag) {
		aBag.validate();
		myBag = aBag;
	}

	/**
	 * Creates a tar bzip'ed bag file.
	 * 
	 * @return A bag.tar.bz2 file
	 * @throws IOException If there is difficulty writing the bag file
	 */
	public File toTarBZip2() throws IOException {
		return BagPackager.toTarBZip2(myBag);
	}

	/**
	 * Returns a directory representing the bag.
	 * 
	 * @return A directory representing the bag
	 * @throws IOException If there is difficulty writing the bag directory
	 */
	public File toDir() throws IOException {
		return myBag.toDir();
	}

	/**
	 * Returns a tar'ed bag file.
	 * 
	 * @return A tar'ed bag file
	 * @throws IOException If there is difficulty writing the bag file
	 */
	public File toTar() throws IOException {
		return BagPackager.toTar(myBag);
	}

	/**
	 * Returns a tar gzip'ed bag file.
	 * 
	 * @return A tar gzip'ed bag file
	 * @throws IOException If there is difficulty writing the bag file
	 */
	public File toTarGz() throws IOException {
		return BagPackager.toTarGz(myBag);
	}

	/**
	 * Returns a zip'ed bag file.
	 * 
	 * @return A zip'ed bag file
	 * @throws IOException If there is difficulty writing the bag file
	 */
	public File toZip() throws IOException {
		return BagPackager.toZip(myBag);
	}
}
