package entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Sandwich.findAll", query = "SELECT i FROM Sandwich i")
})
public class Sandwich implements Serializable {

    private static final long serialVersionUID = 1L;
    private final static String SANDWICH_CUSTOM = "CUSTOM";
    private final static String SANDWICH_SIZE_UNDEFINED = "UNDEFINED";
    private final static String SANDWICH_SIZE_0 = "PETIT";
    private final static String SANDWICH_SIZE_1 = "MOYEN";
    private final static String SANDWICH_SIZE_2 = "GRAND";
    private final static String SANDWICH_SIZE_3 = "OGRE";

    @Id
    private String id;

    private String name;

    private String size;

    private double price;

    @ManyToMany
    @JsonManagedReference
    List<Ingredient> ingredientsList;

    @XmlElement(name="_links")
    @Transient
    private List<Link> links = new ArrayList<>();

    /**
     * Empty constructor
     */
    public Sandwich() {
        size = SANDWICH_SIZE_UNDEFINED;
        name = SANDWICH_CUSTOM;
        price = 0;
        ingredientsList = new ArrayList<>();
    }

    /**
     * Constructor of a sandwich
     * @param ingredients the ingredients in a sandwich
     */
    public Sandwich(String size, Ingredient ... ingredients) {
        price = 0;
        name = SANDWICH_CUSTOM;
        this.size = size;
        ingredientsList = new ArrayList<>();
        for (Ingredient ingredient : ingredients)
            ingredientsList.add(ingredient);

    }

    /**
     * Constructor of a sandwich
     * @param name the sandwich name
     * @param ingredients the ingredients in a sandwich
     */
    public Sandwich(String size, String name, Ingredient ... ingredients) {
        price = 0;
        this.name = name;
        this.size = size;
        ingredientsList = new ArrayList<>();
        for (Ingredient ingredient : ingredients)
            ingredientsList.add(ingredient);

    }

    /**
     * Method that updates a sandwich
     * @param ingredients the ingredients in a sandwich
     * @return the sandiwch
     */
    public Sandwich update(Ingredient ... ingredients){
        ingredientsList.clear();
        for (Ingredient ingredient : ingredients)
            ingredientsList.add(ingredient);
        return this;
    }

     @Override
    public String toString() {
        String res = "Sandwich{" +
                "id='" + id + '\''
                + " Ingredients : [";
        for (Ingredient i: ingredientsList)
            res += i.getName() + " - " + i.getId() + '\'';
        res+= ']';

        return res;
    }

    public void addLink(String uri, String rel) {
        this.links.add(new Link(rel,uri));
    }

    /**
     * Method that returns the ingredients which belongs the category given
     * @param category of the ingredient
     * @return List of Ingredient if it's found else null
     */
    public List<Ingredient> getIngredient(String category) {
        if (!ingredientsList.isEmpty()) {
            List<Ingredient> res = new ArrayList<>();
            for (Ingredient ingredient : ingredientsList)
                if (ingredient.categoryName().equals(category))
                    res.add(ingredient);

            if (!res.isEmpty())
                return res;
        }
        return null;
    }

    /**
     * Helper function that validates the composition of a sandwich
     * @return if it's valid
     */
    public boolean validate() {
        boolean salad =    getIngredient("Salade") != null;
        boolean meat =     getIngredient("Viande") != null;
        boolean coldMeat = getIngredient("Charcuterie") != null;
        boolean cheese =   getIngredient("Fromage") != null;
        boolean sauce =    getIngredient("Sauce") != null;
        boolean crudite = getIngredient("Crudité") != null;
        boolean bread =    getIngredient("Pain") != null;

        if ( salad && meat && coldMeat && cheese && sauce && crudite && bread) {
            switch(this.size) {
                case (SANDWICH_SIZE_0) :
                    if (ingredientsList.size() == 7)
                        return true;
                    break;
                case (SANDWICH_SIZE_1) :
                    if (ingredientsList.size() == 8)
                        return true;
                    break;
                case (SANDWICH_SIZE_2) :
                    if (ingredientsList.size() == 9)
                        return true;
                    break;
                case (SANDWICH_SIZE_3) :
                    if (ingredientsList.size() == 10)
                        return true;
                    break;
            }
        }
        return false;
    }

    /**
     * Method that give the price of a sandwich
     */
    public void calculatePrice() {
        double price = 0;

        switch (size) {
            case (SANDWICH_SIZE_1) :
                price += 1.0;
                break;
            case (SANDWICH_SIZE_2) :
                price += 1.5;
                break;
            case (SANDWICH_SIZE_3) :
                price += 2.0;
                break;
        }

        for (Ingredient ingredient : ingredientsList)
            price += ingredient.getPrice();
        this.price = price;
    }

    /**
     * - Getter and Setter functions -
     */

    public static String getSandwichSize0() {
        return SANDWICH_SIZE_0;
    }

    public static String getSandwichSize1() {
        return SANDWICH_SIZE_1;
    }

    public static String getSandwichSize2() {
        return SANDWICH_SIZE_2;
    }

    public static String getSandwichSize3() {
        return SANDWICH_SIZE_3;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<Link> getLinks() {
        return links;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Ingredient> getIngredientsList() {
        return ingredientsList;
    }

    public void setIngredientsList(List<Ingredient> ingredientsList) {
        this.ingredientsList = new ArrayList<>(ingredientsList);
    }

}
