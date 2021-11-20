package Test;

import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import org.junit.Test;
import org.junit.Assert;

import java.util.List;

public class StateDefinitionTest {

    /***
     * Method is used to check functionality of equals() and hashCode()
     */
    @Test
    public void testEqualsHashCodeForSameObjects() {
        EventDefinition eventDefinition = new EventDefinition("someEvent", "someType");
        List<EventDefinition> eventDefinitionList = null;
        StateDefinition stateDefinition1 = new StateDefinition(1l, "someName", "someDescription",
                "someHook",
                "someTask", "someHook", 1L, 1000L, eventDefinitionList
                , eventDefinition, false, (short) 0);
        StateDefinition stateDefinition2 = new StateDefinition(1l, "someName", "someDescription",
                "someHook",
                "someTask", "someHook", 1L, 1000L, eventDefinitionList
                , eventDefinition, false, (short) 0);
        Assert.assertTrue(
                stateDefinition1.equals(stateDefinition2) && stateDefinition2.equals(stateDefinition1));
        Assert.assertEquals(stateDefinition1.hashCode(), stateDefinition2.hashCode());
    }

    @Test
    public void testEqualsHashCodeForDifferentObjects() {
        EventDefinition eventDefinition = new EventDefinition("someEvent", "someType");
        List<EventDefinition> eventDefinitionList = null;
        StateDefinition stateDefinition1 = new StateDefinition(1l, "someName1", "someDescription1",
                "someHook1",
                "someTask1", "someHook1", 1L, 1000L, eventDefinitionList
                , eventDefinition, false, (short) 0);
        StateDefinition stateDefinition2 = new StateDefinition(1l, "someName2", "someDescription2",
                "someHook2",
                "someTask2", "someHook2", 1L, 1000L, eventDefinitionList
                , eventDefinition, false, (short) 0);
        Assert.assertFalse(
                stateDefinition1.equals(stateDefinition2) && stateDefinition2.equals(stateDefinition1));
        Assert.assertNotEquals(stateDefinition1.hashCode(), stateDefinition2.hashCode());
    }
}