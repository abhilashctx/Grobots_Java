package exception;

public class GBTooManyIterationsError extends GBError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2200153704548616149L;

	@Override
	public String toString() {
		return "a loop had too many iterations";
	}
};