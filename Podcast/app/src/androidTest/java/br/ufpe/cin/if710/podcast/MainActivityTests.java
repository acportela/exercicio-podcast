package br.ufpe.cin.if710.podcast;

import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.ufpe.cin.if710.podcast.ui.MainActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.anything;


/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {

        //Trecho usado para dar permissão ao teste para baixar o arquivo
        //Para verificar seu efeito, se comentado, o botão
        //não muda o texto para "Baixando" e exibe o diolog de permissão
        //Se descomentado, o dialog também é exibido, mas antes vemos o texto do
        //botão mudar. O app em si não pergunta novamente se já tiver permissão
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + getTargetContext().getPackageName()
                            + " android.permission.WRITE_EXTERNAL_STORAGE");
        }

        //Necessário para esperar a lista ser carregada. Caso falhe, aumente o tempo
        Thread.sleep(3000);
    }

    //Testa o click no botao de "Baixar" ou "Tocar de um item da lista (neste caso o primeiro)
    //Testa o uso da permissão de salvar arquivos na memória externa
    //Comente o trecho no before para checar a permissão
    @Test
    public void itemClickMainButtonTest(){
        onData(anything()).inAdapterView(withId(R.id.items)).atPosition(0).onChildView(withId(R.id.item_action)).perform(click());
    }

    //Testa o click no item para abrir a activity de detalhes
    @Test
    public void itemClickOpenDetaisTest(){
        onData(anything()).inAdapterView(withId(R.id.items)).atPosition(0).perform(click());
    }

}
