package com.jparkie.aizoban.views.viewholders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.utils.PaletteBitmapTarget;
import com.jparkie.aizoban.utils.PaletteBitmapTranscoder;
import com.jparkie.aizoban.utils.PaletteUtils;
import com.jparkie.aizoban.utils.wrappers.PaletteBitmapWrapper;
import com.jparkie.aizoban.views.viewholders.base.BaseCatalogueViewHolder;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CatalogueGridViewHolder extends BaseCatalogueViewHolder {
    public static final String TAG = CatalogueListViewHolder.class.getSimpleName();

    private static final int INVALID_COLOUR = -1;
    private static int sDefaultPrimary = INVALID_COLOUR;
    private static int sDefaultAccent = INVALID_COLOUR;

    @InjectView(R.id.catalogueThumbnailImageView)
    ImageView mCatalogueThumbnailImageView;
    @InjectView(R.id.catalogueMaskImageView)
    View mCatalogueMaskView;
    @InjectView(R.id.catalogueFooterLinearLayout)
    LinearLayout mCatalogueFooterLinearLayout;
    @InjectView(R.id.catalogueNameTextView)
    TextView mCatalogueNameTextView;

    public CatalogueGridViewHolder(View itemView) {
        super(itemView);

        ButterKnife.inject(this, itemView);
    }

    @Override
    public void bindMangaToView(Context context, Manga manga) {
        initializeDefaultColours(context);

        setThumbnail(context, manga.getThumbnailUrl());
        setName(manga.getName());
    }

    private void initializeDefaultColours(Context context) {
        if (sDefaultPrimary == INVALID_COLOUR) {
            sDefaultPrimary = context.getResources().getColor(R.color.primaryColor);
        }
        if (sDefaultAccent == INVALID_COLOUR) {
            sDefaultAccent = context.getResources().getColor(R.color.accentColor);
        }
    }

    private void setThumbnail(Context context, String imageUrl) {
        mCatalogueThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER);

        Glide.with(context)
                .load(imageUrl)
                .asBitmap()
                .transcode(new PaletteBitmapTranscoder(), PaletteBitmapWrapper.class)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .animate(android.R.anim.fade_in)
                .placeholder(R.drawable.ic_image_accent_48dp)
                .error(R.drawable.ic_error_accent_48dp)
                .fitCenter()
                .into(new PaletteBitmapTarget(mCatalogueThumbnailImageView) {
                          @Override
                          public void onLoadStarted(Drawable placeholder) {
                              super.onLoadStarted(placeholder);

                              setMaskColour(sDefaultAccent);
                              setFooterColour(sDefaultPrimary);
                          }

                          @Override
                          public void onResourceReady(PaletteBitmapWrapper resource, GlideAnimation<? super PaletteBitmapWrapper> glideAnimation) {
                              mCatalogueThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                              super.onResourceReady(resource, glideAnimation);

                              int extractedColour = PaletteUtils.getColorWithDefault(resource.getPalette(), INVALID_COLOUR);
                              setMaskColour(extractedColour);
                              setFooterColour(extractedColour);
                          }
                      }
                );
    }

    private void setMaskColour(int colour) {
        if (colour != INVALID_COLOUR) {
            GradientDrawable maskDrawable = new GradientDrawable();
            maskDrawable.setColor(colour);
            mCatalogueMaskView.setBackgroundDrawable(maskDrawable);
        }
    }

    private void setFooterColour(int colour) {
        if (colour != INVALID_COLOUR) {
            GradientDrawable footerDrawable = new GradientDrawable();
            footerDrawable.setCornerRadii(new float[]{0.0f, 0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 4.0f, 4.0f});
            footerDrawable.setColor(colour);
            mCatalogueFooterLinearLayout.setBackgroundDrawable(footerDrawable);
        }
    }

    private void setName(String name) {
        mCatalogueNameTextView.setText(name);
    }
}
