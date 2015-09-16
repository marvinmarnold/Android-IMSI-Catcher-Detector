package org.stingraymappingproject.stingwatch.mapping;

import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.stingraymappingproject.stingwatch.BuildConfig;
import org.stingraymappingproject.stingwatch.R;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18)
public class MappingActivityUndetectedTest {

  @Test
  public void clickingButton_shouldChangeResultsViewText() throws Exception {
    MappingActivityUndetected activity = Robolectric.setupActivity(MappingActivityUndetected.class);

    TextView results = (TextView) activity.findViewById(R.id.mapper_safe_description);

    assertThat(results.getText().toString()).isEqualTo("Robolectric Rocks!");
  }

//  @Test
//  public void clickMenuItem_shouldDelegateClickToFragment() {
//    final MappingActivityUndetected activity = Robolectric.setupActivity(MappingActivityUndetected.class);
//
//    shadowOf(activity).clickMenuItem(R.id.item4);
//    assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Clicked Item 4");
//  }
}
