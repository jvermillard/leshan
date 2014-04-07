package leshan.server.lwm2m.observation;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ObserveSpecTest {

    ObserveSpec spec;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testToQueryParamsContainsGreaterThan() {
        Float threshold = 12.1f;
        this.spec = new ObserveSpec.Builder().greaterThan(threshold).build();
        Assert.assertEquals(String.format("gt=%s", threshold), this.spec.toQueryParams()[0]);
    }

    @Test
    public void testToQueryParamsContainsMaxPeriod() {
        int seconds = 60;
        this.spec = new ObserveSpec.Builder().maxPeriod(seconds).build();
        Assert.assertEquals(String.format("pmax=%s", seconds), this.spec.toQueryParams()[0]);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderDetectsInconsistentParams() {
        new ObserveSpec.Builder().minPeriod(5).maxPeriod(2).build();
    }

    @Test
    public void testToQueryParamsContainsStep() {
        Float step = 12.1f;
        this.spec = new ObserveSpec.Builder().step(step).build();
        Assert.assertEquals(String.format("st=%s", step), this.spec.toQueryParams()[0]);
    }

    @Test
    public void testToQueryParamsContainsCancel() {
        this.spec = new ObserveSpec.Builder().cancel().build();
        Assert.assertEquals("cancel", this.spec.toQueryParams()[0]);
    }
}
