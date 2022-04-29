package picross;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Maurice Perry
 */
@SuppressWarnings("Duplicates")
public enum Resizer {
	NEAREST_NEIGHBOR {
		@Override
		public BufferedImage resize(BufferedImage source,
		                            int width, int height) {
			return commonResize(source, width, height,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		}
	},
	BILINEAR {
		@Override
		public BufferedImage resize(BufferedImage source,
		                            int width, int height) {
			return commonResize(source, width, height,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}
	},
	BICUBIC {
		@Override
		public BufferedImage resize(BufferedImage source,
		                            int width, int height) {
			return commonResize(source, width, height,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		}
	},
	PROGRESSIVE_BILINEAR {
		@Override
		public BufferedImage resize(BufferedImage source,
		                            int width, int height) {
			return progressiveResize(source, width, height,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}
	},
	PROGRESSIVE_BICUBIC {
		@Override
		public BufferedImage resize(BufferedImage source,
		                            int width, int height) {
			return progressiveResize(source, width, height,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		}
	},
	AVERAGE {
		@Override
		public BufferedImage resize(BufferedImage source,
		                            int width, int height) {
			Image img2 = source.getScaledInstance(width, height,
					Image.SCALE_AREA_AVERAGING);
			BufferedImage img = new BufferedImage(width, height,
					source.getType());
			Graphics2D g = img.createGraphics();
			try {
				g.drawImage(img2, 0, 0, width, height, null);
			} finally {
				g.dispose();
			}
			return img;
		}
	};

	public abstract BufferedImage resize(BufferedImage source,
	                                     int width, int height);

	private static BufferedImage progressiveResize(BufferedImage source,
	                                               int width, int height, Object hint) {
		int w = Math.max(source.getWidth() / 2, width);
		int h = Math.max(source.getHeight() / 2, height);
		BufferedImage img = commonResize(source, w, h, hint);
		while (w != width || h != height) {
			BufferedImage prev = img;
			w = Math.max(w / 2, width);
			h = Math.max(h / 2, height);
			img = commonResize(prev, w, h, hint);
			prev.flush();
		}
		return img;
	}

	private static BufferedImage commonResize(BufferedImage source,
	                                          int width, int height, Object hint) {
		BufferedImage img = new BufferedImage(width, height,
				source.getType());
		Graphics2D g = img.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g.drawImage(source, 0, 0, width, height, null);
		} finally {
			g.dispose();
		}
		return img;
	}
}