package com.example.pokeapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.pokeapi.models.Pokemon;
import com.example.pokeapi.models.PokemonRespuesta;
import com.example.pokeapi.poke.PokeapiService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "POKEDEX";

    private Retrofit retrofit;
    private RecyclerView recyclerView;
    private PokemonAdapter pokemonAdapter;
    private int offset;

    private boolean aptoParaCargar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        pokemonAdapter = new PokemonAdapter(this);
        recyclerView.setAdapter(pokemonAdapter);

        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0){
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    Log.i(TAG,"child: "+ visibleItemCount);
                    Log.i(TAG,"past: "+ pastVisibleItems);
                    Log.i(TAG,"child: "+ totalItemCount);

                    if (aptoParaCargar){
                        if ( (visibleItemCount + pastVisibleItems) >= totalItemCount){
                            Log.i(TAG,"Llegamos al final");

                            aptoParaCargar = false;
                            offset += 20;
                            obtenerDatos(offset);
                        }
                    }
                }
            }
        });
        retrofit = new Retrofit.Builder().
                baseUrl("https://pokeapi.co/api/v2/").
                addConverterFactory(GsonConverterFactory.create()).build();


        aptoParaCargar = true;
        offset = 0;
        obtenerDatos(offset);
    }

    public void obtenerDatos(int offset){
        PokeapiService service = retrofit.create(PokeapiService.class);
        Call<PokemonRespuesta> pokemonRespuestaCall = service.obtenerListaPokemon(20,offset);

        pokemonRespuestaCall.enqueue(new Callback<PokemonRespuesta>() {
            @Override
            public void onResponse(Call<PokemonRespuesta> call, Response<PokemonRespuesta> response) {
                aptoParaCargar = true;
                if ( response.isSuccessful()){

                    PokemonRespuesta pokemonRespuesta = response.body();
                    ArrayList<Pokemon> listaPokemon = pokemonRespuesta.getResults();

                    for ( Pokemon item : listaPokemon) {
                        Log.i(TAG, "nombre:" + item.getName());
                        Log.i(TAG, "url"+ item.getUrl());
                    }

                    pokemonAdapter.adicionarListaPokemon(listaPokemon);
                }
                else {
                    Log.e(TAG , "onResponse: "+ response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<PokemonRespuesta> call, Throwable t) {
                aptoParaCargar = true;
                Log.e(TAG , "onFailures" + t.getMessage());
            }
        });
    }
}
