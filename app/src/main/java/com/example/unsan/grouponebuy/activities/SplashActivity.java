package com.example.unsan.grouponebuy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.unsan.grouponebuy.R;
import com.example.unsan.grouponebuy.helpers.GlobalProvider;
import com.example.unsan.grouponebuy.helpers.Utf8JsonRequest;
import com.example.unsan.grouponebuy.model.Category;
import com.example.unsan.grouponebuy.model.CategoryImage;
import com.example.unsan.grouponebuy.model.CategoryImageResult;
import com.example.unsan.grouponebuy.model.CategoryList;
import com.example.unsan.grouponebuy.model.CategoryPrimary;
import com.example.unsan.grouponebuy.model.CategoryPrimaryList;
import com.example.unsan.grouponebuy.model.CategorySpecial;
import com.example.unsan.grouponebuy.model.CategorySummary;
import com.example.unsan.grouponebuy.model.FlashSaleResult;
import com.example.unsan.grouponebuy.model.OtherImageResult;
import com.example.unsan.grouponebuy.model.Payload;
import com.example.unsan.grouponebuy.model.Product;
import com.example.unsan.grouponebuy.model.ProductImageId;
import com.example.unsan.grouponebuy.model.SpecialCategoryList;
import com.example.unsan.grouponebuy.model.SpecialImage;
import com.example.unsan.grouponebuy.model.SpecialImageResult;
import com.example.unsan.grouponebuy.network.Constants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SplashActivity extends AppCompatActivity {
    GlobalProvider globalProvider;
    ProgressBar progressBar;
    String language;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
      //  this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_layout);
        progressBar=(ProgressBar) findViewById(R.id.loading_spinner);
        globalProvider=GlobalProvider.getGlobalProviderInstance(getApplicationContext());
        language=Constants.getLanguage(getApplicationContext());

        //setting constants for weeks
        setDeliveryMap();
        getFlashSales();

        //getting single,double and triple layout images for homepage







    }

    private void getSpecialCategories() {
        globalProvider.categorySpecialList.clear();
        Utf8JsonRequest utf8JsonRequest=new Utf8JsonRequest(Request.Method.GET, Constants.specialCategoryUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JsonFactory jsonFactory = new JsonFactory();
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    JsonParser jsonParser = jsonFactory.createParser(response);
                    SpecialCategoryList splcategories = (SpecialCategoryList) objectMapper.readValue(jsonParser, SpecialCategoryList.class);
                   // Log.d("getstatus",splcategories.getStatus()+"");
                    int status=splcategories.getStatus();

                    if(status==0) {
                        List<CategorySpecial> categorySpecialList = splcategories.getCategorySpecialList();
                        for(CategorySpecial categorySpecial:categorySpecialList) {
                            if(categorySpecial.getSequence()!=null&&(categorySpecial.getSequence()==666||categorySpecial.getSequence()==667)) {


                            }
                            else
                            {
                                globalProvider.categorySpecialList.add(categorySpecial);
                            }
                        }
                        getCategoryImage();



                    }

                    else if(status==2)
                    {
                        Toast.makeText(SplashActivity.this,splcategories.getMsg(),Toast.LENGTH_LONG).show();
                    }


                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });
        globalProvider.addRequest(utf8JsonRequest);
    }
    private void getFlashSales()
    {
        Utf8JsonRequest utf8JsonRequest = new Utf8JsonRequest(Request.Method.GET, Constants.flashSaleUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("checkflashresponse",response);
                JsonFactory jsonFactory = new JsonFactory();
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonParser jsonParser = jsonFactory.createParser(response);
                    FlashSaleResult flashSaleResult = (FlashSaleResult) objectMapper.readValue(jsonParser, FlashSaleResult.class);
                    //check flashsale
                 String deadline= flashSaleResult.getPayload().get(0).getDeadline();

                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date saleDate = isoFormat.parse(deadline);
                    globalProvider.saleDate=saleDate;
                    Date today=new Date();
                  Log.d("saleDate",saleDate.toString());
                  Log.d("today",today.toString());

                    if(today.before(saleDate))
                    {
                        //Log.d("flashstuas","wiil be displayed");
                        globalProvider.setFlashSale(flashSaleResult.getPayload().get(0));
                        globalProvider.hasSale=true;


                    }
                    Log.d("hasSale",globalProvider.hasSale+"");

                      //  Log.d("flashstuas","wiil not be displayed");
                    getSingleProducts();




                }
             catch (JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                    e.printStackTrace();
                }
            }}, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String message = null;
                    if (error instanceof NetworkError) {
                        message = "Cannot connect to Internet...Please check your connection!";
                    } else if (error instanceof ServerError) {
                        message = "The server could not be found. Please try again after some time!!";
                    } else if (error instanceof AuthFailureError) {
                        message = "Cannot connect to Internet...Please check your connection!";
                    } else if (error instanceof ParseError) {
                        message = "Parsing error! Please try again after some time!!";
                    } else if (error instanceof NoConnectionError) {
                        message = "Cannot connect to Internet...Please check your connection!";
                    } else if (error instanceof TimeoutError) {
                        message = "Connection TimeOut! Please check your internet connection.";
                    }
                    Toast.makeText(SplashActivity.this,message,Toast.LENGTH_LONG).show();


                }
            });
        globalProvider.addRequest(utf8JsonRequest);
        }



    private void loadSpecialImages() {
        Utf8JsonRequest utf8JsonRequest = new Utf8JsonRequest(Request.Method.GET, Constants.specialImageUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonFactory jsonFactory = new JsonFactory();
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonParser jsonParser = jsonFactory.createParser(response);
                    SpecialImageResult specialImageResult = (SpecialImageResult) objectMapper.readValue(jsonParser, SpecialImageResult.class);
                    globalProvider.specialMImages.clear();
                    if (specialImageResult.getStatus() == 0) {
                        List<SpecialImage> specialImages = new ArrayList<>();
                        specialImages.addAll(specialImageResult.getPayload());

                        globalProvider.specialMImages.addAll(specialImageResult.getPayload());
                        progressBar.setVisibility(View.GONE);
                        Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();



                    }
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        globalProvider.addRequest(utf8JsonRequest);
    }
    private void setDeliveryMap() {
      if(globalProvider.deliveryTiming.isEmpty()) {
          globalProvider.deliveryTiming.put(7, "Sunday");
          globalProvider.deliveryTiming.put(1, "Monday");
          globalProvider.deliveryTiming.put(2, "Tuesday");
          globalProvider.deliveryTiming.put(3, "Wednesday");
          globalProvider.deliveryTiming.put(4, "Thursday");
          globalProvider.deliveryTiming.put(5, "Friday");
          globalProvider.deliveryTiming.put(6, "Saturday");
      }
    }
    private void getCategoryImage()
    {
        globalProvider.categoryImageList.clear();
        String url= Constants.adwordsUrl;
        Utf8JsonRequest utf8JsonRequest=new Utf8JsonRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonFactory jsonFactory=new JsonFactory();
                ObjectMapper objectMapper=new ObjectMapper();
                try {
                    JsonParser jsonParser = jsonFactory.createParser(response);
                    CategoryImageResult categoryImgList = (CategoryImageResult) objectMapper.readValue(jsonParser, CategoryImageResult.class);
                    for(CategoryImage categoryImage:categoryImgList.getPayload())
                    {
                        globalProvider.categoryImageList.add(categoryImage);
                    }
                    getCategories();
                }
                catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        globalProvider.addRequest(utf8JsonRequest);
    }
    private void getSingleProducts()
    {
        globalProvider.singleProductList.clear();
        globalProvider.threeImageLayout.clear();
        String url=Constants.singleProductUrl;
        Utf8JsonRequest utf8JsonRequest=new Utf8JsonRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ObjectMapper objectMapper=new ObjectMapper();
                JsonFactory jsonFactory=new JsonFactory();
                try
                {
                   JsonParser jsonParser= jsonFactory.createParser(response);
                  OtherImageResult otherImageResult=(OtherImageResult) objectMapper.readValue(jsonParser, OtherImageResult.class);
                  if(otherImageResult.getStatus()==0)
                  {
                      Log.d("checksize",otherImageResult.getPayload().size()+"");

                      for(Payload payload:otherImageResult.getPayload())
                      {
                          String category=payload.getCategory();
                          Log.d("categoryname",category);
                          //todo check why space


                          if(!category.contains("two")&&!category.contains("three-bottom")&&!category.contains("three-top"))
                          {

                              // If images cover whole width,i.e single image
                              Product product = payload.getProduct();
                              String imageCover=null;
                              if(language.equals("chinese")) {
                                  imageCover = payload.getImageCh();
                              }
                              else
                                  imageCover=payload.getImageEn();
                              if(category.equals("specialBanner"))
                              {
                                  Log.d("categorypayload",payload.getProduct().getNameEn());
                                  //todo checkhere
                                  Log.d("checkpdname",product.getNameEn());
                                  Log.d("checkimgurl",payload.getImageEn());
                                  ProductImageId productImageId = new ProductImageId(category, imageCover, product,0);
                                  globalProvider.specialBanner=productImageId;

                              }
                              else {


                                  //set viewType as 0
                                  ProductImageId productImageId = new ProductImageId(category, imageCover, product, 0);
                                  //storing it in singleproductlist
                                  globalProvider.singleProductList.add(productImageId);
                              }
                          }
                          else if(category.contains("two-top"))
                          {
                              Product product = payload.getProduct();
                              String imageCover=null;
                              //todo change image acc to lang
                              if(language.equals("english"))
                              {
                                  imageCover=payload.getImageEn();
                              }
                              else
                                  imageCover = payload.getImageCh();


                              //set viewtype as 1

                              ProductImageId productImageId = new ProductImageId(category, imageCover, product,1);
                              globalProvider.doubleProductList.add(productImageId);
                              //store in doubleProductImageList
                          }
                          else if(category.contains("two-bottom")) {


                              Log.d("checkcategorynametwo",category);
                              //if doubles images are displayed




                              Product product = payload.getProduct();
                              String imageCover=null;
                              //todo change image acc to lang
                              if(language.equals("english"))
                              {
                                  imageCover=payload.getImageEn();
                              }
                              else
                              imageCover = payload.getImageCh();


                             //set viewtype as 1

                              ProductImageId productImageId = new ProductImageId(category, imageCover, product,1);
                              globalProvider.doubleProductImageList.add(productImageId);
                              //store in doubleProductImageList
                          }
                          else if(category.contains("three-bottom"))
                          {
                              //if there are three images to be displayed
                              Product product = payload.getProduct();
                              //todo change image acc to lang
                              String imageCover=null;
                              if(language.equals("chinese"))

                               imageCover = payload.getImageCh();
                              else
                                  imageCover=payload.getImageEn();

                             if(category.equals("three-bottom-left")) {
                                 //setviewtype as 2
                                 ProductImageId productImageId = new ProductImageId(category, imageCover, product, 2);
                                 globalProvider.threeImageLayout.add(productImageId);

                             }
                             else if(category.equals("three-bottom-right-up"))
                             {
                                 //setviewtype as 3
                                 ProductImageId productImageId = new ProductImageId(category, imageCover, product, 3);
                                 globalProvider.threeImageLayout.add(productImageId);

                             }
                             else
                             {
                                 //set viewtype as 4
                                 ProductImageId productImageId = new ProductImageId(category, imageCover, product, 4);
                                 globalProvider.threeImageLayout.add(productImageId);

                             }

                             // globalProvider.singleProductList.add(productImageId);

                          }
                          else if(category.equals("three-top"))
                          {
                              //if there are three images to be displayed
                              Product product = payload.getProduct();
                              //todo change image acc to lang
                              String imageCover=null;
                              if(language.equals("chinese"))

                                  imageCover = payload.getImageCh();
                              else
                                  imageCover=payload.getImageEn();

                              if(category.equals("three-top-left")) {
                                  //setviewtype as 2
                                  ProductImageId productImageId = new ProductImageId(category, imageCover, product, 2);
                                  globalProvider.threeTopImageLayout.add(productImageId);

                              }
                              else if(category.equals("three-top-right-up"))
                              {
                                  //setviewtype as 3
                                  ProductImageId productImageId = new ProductImageId(category, imageCover, product, 3);
                                  globalProvider.threeTopImageLayout.add(productImageId);

                              }
                              else
                              {
                                  //set viewtype as 4
                                  ProductImageId productImageId = new ProductImageId(category, imageCover, product, 4);
                                  globalProvider.threeTopImageLayout.add(productImageId);

                              }

                          }


                      }
                      int size=globalProvider.threeImageLayout.size();
                      int twosize=globalProvider.doubleProductImageList.size();
                      int onesize=globalProvider.singleProductList.size();


                      if(globalProvider.categorySpecialList.isEmpty())
                      getSpecialCategories();
                      else
                      {
                          Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                          startActivity(intent);
                          finish();

                      }
                  }

                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        globalProvider.addRequest(utf8JsonRequest);
    }
    private void getCategories() {
        globalProvider.categoryList.clear();
        globalProvider.categoryMap.clear();



        String url= Constants.baseUrlStr+"categoryPrimarys";
        Utf8JsonRequest utf8JsonRequest=new Utf8JsonRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonFactory jsonFactory = new JsonFactory();
                ObjectMapper objectMapper = new ObjectMapper();
                try
                {
                    JsonParser jsonParser = jsonFactory.createParser(response);
                    CategoryPrimaryList categoryPList=(CategoryPrimaryList)objectMapper.readValue(jsonParser, CategoryPrimaryList.class);

                    for(CategoryPrimary categoryPrimary:categoryPList.getPayload())
                    {
                        Log.d("catgoryprimaryname",categoryPrimary.getNameEn());
                        Log.d("categoryprimaryid",categoryPrimary.getId());
                        globalProvider.categoryMap.put(categoryPrimary.getNameEn(),categoryPrimary.getId());
                        CategorySummary categorySummary=new CategorySummary(categoryPrimary.getId(),categoryPrimary.getNameCh(),categoryPrimary.getNameEn(),categoryPrimary.getImage());
                        globalProvider.categoryList.add(categorySummary);




                    }
                    globalProvider.categoryMap.put("All","all");
                    loadSpecialImages();





                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        globalProvider.addRequest(utf8JsonRequest);




      /*  Utf8JsonRequest utf8JsonRequest=new Utf8JsonRequest(Request.Method.GET, Constants.categoryUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JsonFactory jsonFactory = new JsonFactory();
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    JsonParser jsonParser = jsonFactory.createParser(response);
                    CategoryList categories = (CategoryList) objectMapper.readValue(jsonParser, CategoryList.class);
                    Log.d("getstatus",categories.getStatus()+"");
                    int status=categories.getStatus();
                    if(status==0) {


                        globalProvider.categoryList.clear();

                        globalProvider.categoryList.addAll(categories.getCategoryList());

                        for(Category category:globalProvider.categoryList)
                        {
                            Log.d("checkcategoryname",category.getNameEn());
                            globalProvider.categoryMap.put(category.getNameEn(),category.getId());
                        }
                        globalProvider.categoryMap.put("All","all");
                        loadSpecialImages();
                    }
                    else if(status==2)
                    {
                        Toast.makeText(SplashActivity.this,"Something is wrong",Toast.LENGTH_LONG).show();
                    }


                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("checkerror",error.toString());

            }
        });
        globalProvider.addRequest(utf8JsonRequest);
         */

    }


}
