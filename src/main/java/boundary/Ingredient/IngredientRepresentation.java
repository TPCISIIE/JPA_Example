package boundary.Ingredient;

import boundary.Category.CategoryRepresentation;
import boundary.Category.CategoryResource;
import boundary.Sandwich.SandwichResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import control.DatabaseSeeder;
import entity.AccountRole;
import entity.Category;
import entity.Ingredient;
import provider.Secured;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;



@Produces(MediaType.APPLICATION_JSON)
@Stateless
@Api(value = "/ingredients", description = "Ingredients management")
@Path("/ingredients")
public class IngredientRepresentation {

    @EJB
    IngredientResource ingredientResource;

    @EJB
    CategoryResource categoryResource;

    @EJB
    SandwichResource sandwichResource;
    
    @GET
    @ApiOperation(value = "Get all ingredients", notes = "Access : Guest, Customer and Admin")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response getIngredients(@Context UriInfo uriInfo) {
        DatabaseSeeder.feedCatalog(ingredientResource, categoryResource, sandwichResource);
        List<Ingredient> ingredients = ingredientResource.findAll();

        ingredients.stream().forEach(ingredient -> {
            ingredient.getLinks().clear();
            ingredient.addLink(this.getUriForSelfCategory(uriInfo,ingredient.getCategory()),"category");
        });

        GenericEntity<List<Ingredient>> list = new GenericEntity<List<Ingredient>>(ingredients){};
        return Response.ok(list, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/{ingredientId}")
    @ApiOperation(value = "Get an ingredient by its id", notes = "Access : Guest, Customer and Admin")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response getIngredient(@PathParam("ingredientId") String ingredientId) {
        DatabaseSeeder.feedCatalog(ingredientResource, categoryResource, sandwichResource);

        Ingredient ingredient = ingredientResource.findById(ingredientId);
        if (ingredient != null)
            return Response.ok(ingredient, MediaType.APPLICATION_JSON).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/name/{ingredientName}")
    @ApiOperation(value = "Get ingredients by their name", notes = "Access : Guest, Customer and Admin - Can return one or many ingredients")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response getIngredientByName(@PathParam("ingredientName") String ingredientName) {
        DatabaseSeeder.feedCatalog(ingredientResource, categoryResource, sandwichResource);

        List<Ingredient> ingredients = ingredientResource.findByName(ingredientName);

        if (ingredients.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();

        GenericEntity<List<Ingredient>> list = new GenericEntity<List<Ingredient>>(ingredients){};
        return Response.ok(list, MediaType.APPLICATION_JSON).build();

    }


    @ApiOperation(value = "Edit an ingredient", notes = "Access : Admin only - Have to fill one field at least")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 304, message = "Not Modified"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @PUT
    @Secured({AccountRole.ADMIN})
    @Path("/{ingredientId}")
    public Response editIngredient(
            @PathParam("ingredientId") String ingredientId,
            @FormParam("name") String name,
            @FormParam("categoryId") String categoryId,
            @FormParam("price") double price,
            @FormParam("description") String description
    ) {
        Ingredient ingredient = ingredientResource.findById(ingredientId);

        boolean isFormEmpty = (name == null && categoryId == null && Double.toString(price) == null && description == null);

        if (ingredient == null || isFormEmpty)
            return Response.notModified().build();

        String n = (name == null) ? ingredient.getName() : name ;
        String d = (description == null) ? ingredient.getDescription() : description ;
        double p = (Double.toString(price) == null) ? ingredient.getPrice() : price;
        Category c = (categoryId == null) ? ingredient.getCategory() : categoryResource.findById(categoryId);

        if (c == null || ingredientResource.update(ingredient.update(c,n,p,d)) == null)
            return Response.notModified().build();

        return Response.ok().build();
    }

    @DELETE
    @Path("/{ingredientId}")
    @ApiOperation(value = "Delete an ingredient", notes = "Access : Admin only")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @Secured({AccountRole.ADMIN})
    public Response deleteIngredient(@PathParam("ingredientId") String ingredientId) {
        if (ingredientResource.delete(ingredientId))
            return Response.status(204).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation(value = "Create an ingredient", notes = "Access : Admin only")
    @Secured({AccountRole.ADMIN})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response add (
            @FormParam("name") String name,
            @FormParam("categoryId") String categoryId,
            @FormParam("price") double price,
            @FormParam("description") String description
    ) {

        Ingredient ingredient = new Ingredient(categoryResource.findById(categoryId),name,price,description);

        if (ingredientResource.insert(ingredient) == null)
            return Response.status(Response.Status.NOT_FOUND).build();

       return Response.ok().build();
    }

    @POST
    @Path("/breads")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation(value = "Create a bread ingredient", notes = "Access : Admin only")
    @Secured({AccountRole.ADMIN})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 417, message = "Expectation failed"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response addBread (
            @FormParam("name") String name,
            @FormParam("price") double price,
            @FormParam("description") String description
    ) {

        if (name == null || Double.toString(price) == null || description == null)
            return Response.status(Response.Status.EXPECTATION_FAILED).build();

        List<Category> query = categoryResource.findByName("Pain");
        Category breadCategory = (query.size() > 0) ? query.get(0) : categoryResource.insert(new Category("Pain"));
        ingredientResource.insert(new Ingredient(breadCategory,name,price,description));

        return Response.ok().build();
    }

    private String getUriForSelfCategory(UriInfo uriInfo, Category category) {
        return uriInfo.getBaseUriBuilder()
                .path(CategoryRepresentation.class)
                .path(category.getId())
                .build()
                .toString();
    }
}
