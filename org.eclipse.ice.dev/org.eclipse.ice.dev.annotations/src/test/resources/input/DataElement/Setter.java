import org.eclipse.ice.dev.annotations.DataElement;
import org.eclipse.ice.dev.annotations.DataField;

@DataElement(name = "Test")
public class Setter {
	@DataField(setter = false)
	public int test;
}
