package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.Model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Projet;
import miage.fr.gestionprojet.models.dao.DaoAction;
import miage.fr.gestionprojet.models.dao.DaoFormation;
import miage.fr.gestionprojet.models.dao.DaoProjet;
import miage.fr.gestionprojet.models.dao.DaoSaisieCharge;
import miage.fr.gestionprojet.outils.Outils;

public class ActivityDetailsProjet extends AppCompatActivity {

    private final static String RESSOURCES = "Avancement des saisies";
    private final static String FORMATIONS = "Avancement des formations";
    private final static String PLANNING = "Planning détaillé";
    private final static String BUDGET = "Suivi du budget";
    public final static String PROJET = "projet visu";
    private ListView liste = null;
    public final static String EXTRA_INITIAL = "initial";
    private ArrayList <String> lstActions;
    private Projet proj;
    public String initialUtilisateur =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_projet);

        Intent intent = getIntent();
        long id = intent.getLongExtra(MainActivity.EXTRA_PROJET,0);
        initialUtilisateur = intent.getStringExtra(MainActivity.EXTRA_INITIAL);

        // s'il n'y pas d'erreur, un projet est sélectionné
        if (id > 0) {
            // on récupère toutes les données de ce projet
            proj = Model.load(Projet.class, id);

            // on récupère les différents élements de la vue
            TextView txtNomProj = (TextView) findViewById(R.id.textViewNomProjet);

            // on alimente ces différents éléments
            txtNomProj.setText(proj.getNom());

            // on constitue une liste d'action
            liste = (ListView) findViewById(R.id.listViewAction);
            lstActions = new ArrayList<String>();
            lstActions.add(RESSOURCES);
            lstActions.add(FORMATIONS);
            lstActions.add(PLANNING);
            lstActions.add(BUDGET);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lstActions);
            liste.setAdapter(adapter);


            liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Intent intent;
                    switch (position) {
                        case 0:
                            intent = new Intent(ActivityDetailsProjet.this, ActivityIndicateursSaisieCharge.class);
                            intent.putExtra(EXTRA_INITIAL,initialUtilisateur);
                            intent.putExtra(PROJET, proj.getId());
                            startActivity(intent);
                            break;
                        case 1:
                            intent = new Intent(ActivityDetailsProjet.this, FormationsActivity.class);
                            intent.putExtra(EXTRA_INITIAL,initialUtilisateur);
                            startActivity(intent);
                            break;
                        case 2:
                            intent = new Intent(ActivityDetailsProjet.this, ActionsActivity.class);
                            intent.putExtra(EXTRA_INITIAL, initialUtilisateur);
                            intent.putExtra(PROJET, proj.getId());
                            startActivity(intent);
                            break;
                        case 3:
                            intent = new Intent(ActivityDetailsProjet.this, ActivityBudget.class);
                            intent.putExtra(EXTRA_INITIAL,initialUtilisateur);
                            intent.putExtra(PROJET, proj.getId());
                            startActivity(intent);
                            break;

                    }

                }
            });

            //avancement du projet
            ProgressBar progress = (ProgressBar) findViewById(R.id.progressBarProjet);
            int nbActionsRealise = DaoAction.getActionRealiseesByProjet(this.proj.getId()).size();
            int nbActions = DaoAction.getAllActionsByProjet(this.proj.getId()).size();
            int ratioBudget = Outils.calculerPourcentage(nbActionsRealise,nbActions);
            progress.setProgress(ratioBudget);

            //action lors du clic sur le bouton action
            final Button buttonSaisies = (Button) findViewById(R.id.btnSaisies);
            buttonSaisies.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityDetailsProjet.this, ActivityIndicateursSaisieCharge.class);
                    intent.putExtra(EXTRA_INITIAL,initialUtilisateur);
                    intent.putExtra(PROJET, proj.getId());
                    startActivity(intent);
                }
            });

            //action lors du clic sur le bouton formation
            final Button buttonFormations = (Button) findViewById(R.id.btnFormations);
            buttonFormations.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityDetailsProjet.this, FormationsActivity.class);
                    intent.putExtra(EXTRA_INITIAL,initialUtilisateur);
                    startActivity(intent);
                }
            });

            //action lors du clic sur le bouton budget
            final Button buttonBudget = (Button) findViewById(R.id.btnBudget);
            buttonBudget.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityDetailsProjet.this, ActivityBudget.class);
                    intent.putExtra(EXTRA_INITIAL,initialUtilisateur);
                    intent.putExtra(PROJET, proj.getId());
                    startActivity(intent);
                }
            });

            //proportion de durée
            Date dateFin = DaoProjet.getDateFin(this.proj.getId());
            long dureeRestante = Outils.dureeEntreDeuxDate(Calendar.getInstance().getTime(),dateFin);
            long dureeTotal = Outils.dureeEntreDeuxDate(DaoProjet.getDateDebut(this.proj.getId()),DaoProjet.getDateFin(this.proj.getId()));
            int ratioDuree  = Outils.calculerPourcentage(dureeRestante,dureeTotal);

            //détermination de la couleur du bouton budget en fonction du temps restant et du nombre d'actions déjà réalisées
            if(ratioDuree<100-ratioBudget){
                buttonBudget.setBackgroundColor(Color.RED);
            }else if(ratioDuree>100-ratioBudget){
                buttonBudget.setBackgroundColor(Color.GREEN);
            }else{
                buttonBudget.setBackgroundColor(Color.YELLOW);
            }

            //détermination de la couleur du bouton formation
            float avancementTotalFormation = DaoFormation.getAvancementTotal(this.proj.getId());
            int ratioFormation = Outils.calculerPourcentage(avancementTotalFormation,100);
            if(ratioDuree<100-ratioFormation){
                buttonFormations.setBackgroundColor(Color.RED);
            }else if(ratioDuree>100-ratioFormation){
                buttonFormations.setBackgroundColor(Color.GREEN);
            }else{
                buttonFormations.setBackgroundColor(Color.YELLOW);
            }

            //détermination de la couleur du bouton action
            int nbUniteesSaisies = DaoSaisieCharge.getNbUnitesSaisies(this.proj.getId());
            int nbUniteesCibles = DaoSaisieCharge.getNbUnitesCibles(this.proj.getId());
            int ratioSaisies = Outils.calculerPourcentage(nbUniteesSaisies,nbUniteesCibles);
            if(ratioDuree<100-ratioSaisies){
                buttonSaisies.setBackgroundColor(Color.RED);
            }else if(ratioDuree>100-ratioSaisies){
                buttonSaisies.setBackgroundColor(Color.GREEN);
            }else{
                buttonSaisies.setBackgroundColor(Color.YELLOW);
            }
        }

    }

    //ajout du menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.initial_utilisateur, menu);
        menu.findItem(R.id.initial_utilisateur).setTitle(initialUtilisateur);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.initial_utilisateur:
                return true;
            case R.id.charger_donnees:
                Intent intent = new Intent(ActivityDetailsProjet.this, ChargementDonnees.class);
                intent.putExtra(EXTRA_INITIAL, (initialUtilisateur));
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void definirCouleurBouton(){

    }
}
