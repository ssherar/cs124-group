package uk.ac.aber.dcs.cs124group.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.Point;

import uk.ac.aber.dcs.cs124group.model.ClassModel;
import uk.ac.aber.dcs.cs124group.model.Relationship;
import uk.ac.aber.dcs.cs124group.model.RelationshipType;

public class RelationshipController extends DiagramListener implements ActionListener {
	private Relationship model;
	private Point movedPoint;
	
	public RelationshipController(Relationship m) {
		this.model = m;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String c = arg0.getActionCommand();
		if(c.equals("Aggregation")) {
			model.setType(RelationshipType.AGGREGATION);
		}
		else if (c.equals("Composition")) {
			model.setType(RelationshipType.COMPOSITION);
		}
		else if (c.equals("Inheritance")) {
			model.setType(RelationshipType.INHERITANCE);
		}
		else if (c.equals("Uses")) {
			model.setType(RelationshipType.USES);
		}
		else if (c.equals("Implements")) {
			model.setType(RelationshipType.IMPLEMENTS);
		}
		else if (c.equals("Invert")) {
			ClassModel to, from;
			to = model.getGoingTo();
			from = model.getGoingFrom();
			model.setGoingTo(from);
			model.setGoingFrom(to);
			model.setInverted();
		}
		else if (c.equals("Delete")) {
			model.remove();
		}
		

	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		movedPoint = null;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(movedPoint == null) {
			for(Point p : this.model.getPoints()) {
				if(p.distance(e.getPoint()) < 10) {
					movedPoint = p;
				}
			}
		}
		if(movedPoint != null) {
			movedPoint.move(e.getX() - movedPoint.x, e.getY() - movedPoint.y);
			this.model.pointMoved();
		}
	}

}
