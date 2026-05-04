package fr.upc.cr1a.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import fr.upc.cr1a.controller.data.*;

@DisplayName("Tests de StringController")
public class StringControllerTest {


    @Test
    @DisplayName("Un mot sans espace est retourné tel quel")
    public void withoutSpace_noSpaces_returnsUnchanged() {
        String result = StringController.withoutSpace("hello");
        assertEquals("hello", result);
    }

    @Test
    @DisplayName("Un seul caractère sans espace est retourné tel quel")
    public void withoutSpace_singleChar_returnsUnchanged() {
        String result = StringController.withoutSpace("a");
        assertEquals("a", result);
    }

    @Test
    @DisplayName("Une chaîne déjà propre avec plusieurs mots est retournée telle quelle")
    public void withoutSpace_multiWordNoLeadingTrailing_returnsUnchanged() {
        String result = StringController.withoutSpace("hello world");
        assertEquals("hello world", result);
    }

    @Test
    @DisplayName("Un espace en début est supprimé")
    public void withoutSpace_oneLeadingSpace_removesIt() {
        String result = StringController.withoutSpace(" hello");
        assertEquals("hello", result);
    }

    @Test
    @DisplayName("Plusieurs espaces en début sont tous supprimés")
    public void withoutSpace_multipleLeadingSpaces_removesAll() {
        String result = StringController.withoutSpace("   hello");
        assertEquals("hello", result);
    }


    @Test
    @DisplayName("Les espaces en début ET en fin sont tous supprimés (si bug corrigé)")
    public void withoutSpace_leadingAndTrailingSpaces_removesAll() {
        String result = StringController.withoutSpace("  hello");
        assertEquals("hello", result);
    }


    @Test
    @DisplayName("Une chaîne avec uniquement des espaces internes est conservée")
    public void withoutSpace_internalSpacesOnly_returnsUnchanged() {
        String result = StringController.withoutSpace("hello world test");
        assertEquals("hello world test", result);
    }

    @Test
    @DisplayName("Un email sans espaces est retourné tel quel")
    public void withoutSpace_email_noSpaces_returnsUnchanged() {
        String result = StringController.withoutSpace("user@example.com");
        assertEquals("user@example.com", result);
    }

    @Test
    @DisplayName("Un espace en début d'un email est supprimé")
    public void withoutSpace_email_leadingSpace_removesIt() {
        String result = StringController.withoutSpace(" user@example.com");
        assertEquals("user@example.com", result);
    }
}