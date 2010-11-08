package info.freelibrary.bagit;

import java.io.File;
import java.io.IOException;

public class ValidatedBag {
	
	private Bag myBag;
	
	ValidatedBag(Bag aBag) {
		myBag = aBag;
	}
	
	public File toTarBZip2() throws IOException {
		return BagPackager.toTarBZip2(myBag);
	}
	
	public File toDir() {
		return myBag.myDir;
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
	
	protected void finalize() throws Throwable {
		super.finalize();
		myBag.finalize();
	}
}
