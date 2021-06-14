package com.revealprecision.reveal.interactor;

import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.junit.Test;
import com.revealprecision.reveal.BaseUnitTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class RevealJsonFormInteractorTest extends BaseUnitTest {

    @Test
    public void testGetInstance() {
        assertThat(RevealJsonFormInteractor.getInstance(), is(instanceOf(JsonFormInteractor.class)));
    }

}