package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.TutorialModel;
import fr.upc.cr1a.model.TutorialModel.TutorialMode;
import fr.upc.cr1a.view.TutorialView;
import fr.upc.cr1a.view.TutorialModeSelectionView;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TutorialController {
    private TutorialModel model;
    private TutorialView view;
    private TutorialModeSelectionView modeSelectionView;

    public TutorialController(TutorialModel model, TutorialView view, TutorialModeSelectionView modeSelectionView) {
        this.model = model;
        this.view = view;
        this.modeSelectionView = modeSelectionView;

        //lsteners pour les triangles de navigation du tutoriel
        view.getNextButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                model.nextPage();
                view.repaint();
            }
        });

        view.getPreviousButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                model.previousPage();
                view.repaint();
            }
        });

        view.getCloseButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                closeTutorial();
            }
        });

        //lsteners pour la sélection du mode
        modeSelectionView.getClassicButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectMode(TutorialMode.CLASSIC);
            }
        });

        modeSelectionView.getActionButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectMode(TutorialMode.ACTION);
            }
        });
    }

    private void selectMode(TutorialMode mode) {
        model.setMode(mode);
        modeSelectionView.setVisible(false);
        view.setVisible(true);
        view.repaint();
    }

    public void showTutorial() {
        model.reset();
        modeSelectionView.setVisible(true);
        view.setVisible(false);
        modeSelectionView.repaint();
    }

    public void closeTutorial() {
        view.setVisible(false);
        modeSelectionView.setVisible(false);
    }
}
