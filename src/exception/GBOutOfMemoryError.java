package exception;

public class GBOutOfMemoryError extends GBError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5674016878687100947L;

	@Override
	public String ToString() {
		return "out of memory";
	}
};