package Kevin.Peyton.Game.Platform.Demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "developers")
public class Developer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "developer_id")
	private Integer id;

	@Column(name = "dev_name", nullable = false)
	private String devName;

	@Column(name = "country")
	private String country;

	@Column(name = "owner_user_id")
	private Integer ownerUserId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Integer getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(Integer ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

}
