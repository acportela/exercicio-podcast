package br.ufpe.cin.if710.podcast;

import org.junit.Before;
import org.junit.Test;
//import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;

import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    ItemFeed a;
    ItemFeed b;

    List<ItemFeed> itens;

    @Before
    public void creation(){

        a = new ItemFeed("TituloA","http://www.google.com","20/10/2015","Podcast sobre Newton","http://www.downloadlink.com",null,null);
        b = new ItemFeed("TituloB","http://www.yahoo.com","30/11/2016","Podcast sobre Pascal","http://www.asd.com",null,null);
        itens = new ArrayList<>();
        itens.add(a);
        itens.add(b);



        //PodcastDBHelper dbHelper = Mockito.mock(PodcastDBHelper.class);

    }
}