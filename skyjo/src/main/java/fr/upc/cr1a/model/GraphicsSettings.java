package fr.upc.cr1a.model;

/**
 * Classe pour gerer les parametres graph du jeu
 */
public final class GraphicsSettings {
	public static final boolean IS_LINUX = System.getProperty("os.name", "").toLowerCase().contains("linux"); // je remet IS_LINUX ici c'est plus logique

	public enum GraphicsProfile {
		BAS,
		HAUT
	}

	private static GraphicsProfile profile = IS_LINUX ? GraphicsProfile.BAS : GraphicsProfile.HAUT; // sur linux, par defaut, on commence toujours en BAS sinon en HAUT

	private GraphicsSettings() {}

	public static GraphicsProfile getProfile() {
		return profile;
	}

	public static boolean isHighQuality() {
		return profile == GraphicsProfile.HAUT;
	}

	public static void toggleProfile() {
		profile = (profile == GraphicsProfile.HAUT) ? GraphicsProfile.BAS : GraphicsProfile.HAUT;
	}

	public static void setHighQuality(boolean highQuality) {
		profile = highQuality ? GraphicsProfile.HAUT : GraphicsProfile.BAS;
	}
}
